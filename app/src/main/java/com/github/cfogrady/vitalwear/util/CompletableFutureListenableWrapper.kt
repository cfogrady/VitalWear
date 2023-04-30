package com.github.cfogrady.vitalwear.util

import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class CompletableFutureListenableWrapper<T: Any>(private val completableFuture: CompletableFuture<T>) : ListenableFuture<T> {
    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        return completableFuture.cancel(mayInterruptIfRunning)
    }

    override fun isCancelled(): Boolean {
        return completableFuture.isCancelled
    }

    override fun isDone(): Boolean {
        return completableFuture.isDone
    }

    override fun get(): T {
        return completableFuture.get()
    }

    override fun get(timeout: Long, unit: TimeUnit?): T {
        return completableFuture.get(timeout, unit)
    }

    override fun addListener(listener: Runnable, executor: Executor) {
        completableFuture.thenAcceptAsync({listener.run()}, executor)
    }
}