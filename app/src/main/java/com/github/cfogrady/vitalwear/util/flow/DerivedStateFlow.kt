package com.github.cfogrady.vitalwear.util.flow

import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow

class DerivedStateFlow<T, R>(
    private val underlying: StateFlow<T>,
    private val transformer: (T)->R,
): StateFlow<R> {
    private var internalValue: R = transformer(underlying.value)
    override val value: R
        get() = internalValue

    override val replayCache: List<R>
        get() = listOf(value)

    override suspend fun collect(collector: FlowCollector<R>): Nothing {
        underlying.collect {
            collector.emit(transformer(it))
        }
    }
}

fun <T, R> StateFlow<T>.mapState(transform: (T)->R): StateFlow<R> {
    return DerivedStateFlow(this, transform)
}
