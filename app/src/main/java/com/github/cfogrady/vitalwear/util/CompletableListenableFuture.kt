package com.github.cfogrady.vitalwear.util

import com.google.common.collect.Lists
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit

class CompletableListenableFuture<T: Any> : ListenableFuture<T> {
    private var completed = false
    private  lateinit var result : T
    private  val listners = Lists.newArrayList<Pair<Runnable, Executor>>()

    fun complete(value: T) {
        result = value
        completed = true
        while(!listners.isEmpty()) {
            val executablePair = listners.removeLast()
            executablePair.second.execute(executablePair.first)
        }
    }

    override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
        TODO("Not yet implemented")
    }

    override fun isCancelled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isDone(): Boolean {
        return completed
    }

    override fun get(): T {
        return result
    }

    override fun get(timeout: Long, unit: TimeUnit?): T {
        TODO("Not yet implemented")
    }

    override fun addListener(listener: Runnable, executor: Executor) {
        if(completed) {
            executor.execute(listener)
        } else {
            listners.add(Pair(listener, executor))
        }
    }
}