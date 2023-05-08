package com.github.cfogrady.vitalwear.util

import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class DeferredListenableWrapper<T : Any>(val deferred: Deferred<T>) : ListenableFuture<T> {
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        deferred.cancel()
        return true
    }

    override fun isCancelled(): Boolean {
        return deferred.isCancelled
    }

    override fun isDone(): Boolean {
        return deferred.isCompleted
    }

    override fun get(): T {
        return runBlocking {
            deferred.await()
        }
    }

    override fun get(timeout: Long, unit: TimeUnit?): T {
        return runBlocking {
            deferred.await()
        }
    }

    override fun addListener(listener: Runnable, executor: Executor) {
        deferred.invokeOnCompletion {
            executor.execute(listener)
        }
    }
}