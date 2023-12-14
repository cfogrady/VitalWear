package com.github.cfogrady.vitalwear.card

import com.google.common.collect.ImmutableSet

class CardLoaderObserverImpl(private val observer: () -> Unit,
                             override var cardsBeingLoaded: ImmutableSet<String>,
                             private val onStop: (CardLoaderObserverImpl) -> Unit,
) : CardLoaderObserver {
    fun receiveObservation() {
        observer.invoke()
    }

    override fun stopObserving() {
        onStop.invoke(this)
    }
}