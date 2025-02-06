package com.github.cfogrady.vitalwear.util.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TransformedStateFlow<T, R>(
    private val underlying: StateFlow<T>,
    initialValue: R, // we need an initial value because the transform is a suspend function.
    private val transform: suspend FlowCollector<R>.(value: T)->Unit,
): StateFlow<R> {
    private var internalValue: R = initialValue
    override val value: R
        get() = internalValue

    override val replayCache: List<R>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        val valueCollector = FlowCollector<R> { value ->
            internalValue = value
            collector.emit(value)
        }
        underlying.collect {
            valueCollector.transform(it)
        }
    }
}

fun <T,R> StateFlow<T>.transformState(
    initialValue: R,
    transformer: suspend FlowCollector<R>.(value: T)->Unit
): StateFlow<R> {
    return TransformedStateFlow(this, initialValue, transformer)
}
