package com.github.cfogrady.vitalwear.util.flow

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CombinedStateFlow<T , R>(
    val flowsToCombine: Array<out StateFlow<T>>,
    val transform: (ArrayList<T>)->R,
): StateFlow<R> {
    private var internalValue: R

    init {
        val array = ArrayList<T>(flowsToCombine.size)
        for(flow in flowsToCombine) {
            array.add(flow.value)
        }
        internalValue = transform(array)
    }

    override val replayCache: List<R>
        get() = listOf(value)
    override val value: R
        get() = internalValue

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        val array = ArrayList<T>(flowsToCombine.size)
        val arrayFlow = MutableSharedFlow<ArrayList<T>>()
        for(i in flowsToCombine.indices) { // we need indices to know which array index to change
            array.add(flowsToCombine[i].value)
            // We launch new coroutines, but use the current context. This allows async collection,
            // and should make all the new coroutines children of the current and thus cancelled
            // when the current coroutine context is cancelled.
            CoroutineScope(currentCoroutineContext()).launch {
                flowsToCombine[i].collect{
                    array[i] = it
                    arrayFlow.emit(array)
                }
            }
        }
        arrayFlow.collect {
            val result = transform(it)
            internalValue = result
            collector.emit(result)
        }
    }
}

fun <T1, T2, R> combineStates(
    stateFlow1: StateFlow<T1>,
    stateFlow2: StateFlow<T2>,
    transform: (T1, T2) -> R
): StateFlow<R> {
    return CombinedStateFlow(arrayOf(stateFlow1, stateFlow2), {
        transform(it[0] as T1, it[1] as T2)
    })
}

fun <T1, T2, T3, R> combineStates(
    stateFlow1: StateFlow<T1>,
    stateFlow2: StateFlow<T2>,
    stateFlow3: StateFlow<T3>,
    transform: (T1, T2, T3) -> R
): StateFlow<R> {
    return CombinedStateFlow(arrayOf(stateFlow1, stateFlow2, stateFlow3), {
        transform(it[0] as T1, it[1] as T2, it[2] as T3)
    })
}