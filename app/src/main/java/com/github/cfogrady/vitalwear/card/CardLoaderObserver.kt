package com.github.cfogrady.vitalwear.card

import com.google.common.collect.ImmutableSet

interface CardLoaderObserver {
    val cardsBeingLoaded: ImmutableSet<String>
    fun stopObserving()
}