package com.github.cfogrady.vitalwear.debug

import android.util.Log
import java.time.LocalDateTime
import java.util.LinkedList

class ExceptionService {
    companion object {
        const val TAG = "ExceptionService"
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
            Log.e(TAG, "${exception.first}: ", exception.second)
        }
    }
}