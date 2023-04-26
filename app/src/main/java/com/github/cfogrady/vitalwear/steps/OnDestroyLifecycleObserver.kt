package com.github.cfogrady.vitalwear.steps

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner

class OnDestroyLifecycleObserver(private val onDestroy: () -> Unit): LifecycleEventObserver {
    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        // Destroyed is called when we exit the activity
        if(event.targetState == Lifecycle.State.DESTROYED) {
            onDestroy.invoke()
            source.lifecycle.removeObserver(this)
        }
    }
}