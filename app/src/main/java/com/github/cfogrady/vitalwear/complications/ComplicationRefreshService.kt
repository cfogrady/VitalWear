package com.github.cfogrady.vitalwear.complications

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.*
import com.github.cfogrady.vitalwear.VitalWearApp

class ComplicationRefreshService(private val applicationContext: Context, private val complicationState: PartnerComplicationState) {

    var partnerComplicationUpdater: PartnerComplicationUpdater? = null

    companion object {
        const val PARTNER_WORK_TAG = "PartnerComplicationWork"
        const val TAG = "ComplicationRefreshService"
    }

    fun refreshVitalsComplication() {
        val component = ComponentName(applicationContext, VitalsComplicationService::class.java)
        val complicationDataSourceUpdateRequester =
            ComplicationDataSourceUpdateRequester.create(
                context = applicationContext,
                complicationDataSourceComponent = component
            )
        complicationDataSourceUpdateRequester.requestUpdateAll()
    }

    fun startupPartnerComplications() {
        synchronized(this) {
            if (partnerComplicationUpdater == null) {
                Log.i(TAG, "Starting partner complication updates")
                partnerComplicationUpdater = PartnerComplicationUpdater(applicationContext)
                partnerComplicationUpdater!!.setupComplicationUpdate()
            } else {
                Log.i(TAG, "Attempted to start partner complication updates, but updater is already running")
            }
        }
    }

    class PartnerComplicationUpdater(private val applicationContext: Context) {
        var cancelled = false

        fun setupComplicationUpdate() {
            Handler(Looper.getMainLooper()!!).postDelayed({
                updateComplicationState()
                refreshComplication()
                if(!cancelled) {
                    setupComplicationUpdate()
                }
            }, 500)
        }

        private fun updateComplicationState() {
            val state = (applicationContext as VitalWearApp).partnerComplicationState
            state.spriteIndex++
            if(state.spriteIndex > 1) {
                state.spriteIndex = 0
            }
        }

        private fun refreshComplication() {
            val component = ComponentName(applicationContext, PartnerComplicationService::class.java)
            val complicationDataSourceUpdateRequester =
                ComplicationDataSourceUpdateRequester.create(
                    context = applicationContext,
                    complicationDataSourceComponent = component
                )
            complicationDataSourceUpdateRequester.requestUpdateAll()
        }
    }

    fun cancel(workManager: WorkManager = WorkManager.getInstance(applicationContext)) {
        workManager.cancelAllWorkByTag(PARTNER_WORK_TAG)
    }
}