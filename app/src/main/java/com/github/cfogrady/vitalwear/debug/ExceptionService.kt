package com.github.cfogrady.vitalwear.debug

import timber.log.Timber
import java.time.LocalDateTime
import java.util.LinkedList

class ExceptionService {
    companion object {
        var instance: ExceptionService? = null
    }

    init {
        instance = this
    }

    val exceptions : LinkedList<Pair<LocalDateTime, java.lang.Exception>> = LinkedList()

    fun logException(exception: java.lang.Exception) {
        exceptions.addFirst(Pair(LocalDateTime.now(), exception))
        if(exceptions.size > 10) {
            exceptions.removeLast()
        }
    }

    fun logOutExceptions() {
        for(exception in exceptions) {
            Timber.e("${exception.first}: ", exception.second)
        }
    }
}