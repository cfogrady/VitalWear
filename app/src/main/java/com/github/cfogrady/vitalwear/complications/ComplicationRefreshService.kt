package com.github.cfogrady.vitalwear.complications

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.work.*
import com.github.cfogrady.vitalwear.VitalWearApp
import timber.log.Timber

class ComplicationRefreshService(private val applicationContext: Context, private val complicationState: PartnerComplicationState) {

    var partnerComplicationUpdater: PartnerComplicationUpdater? = null

    companion object {
        const val PARTNER_WORK_TAG = "PartnerComplicationWork"
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
                Timber.i("Starting partner complication updates")
                partnerComplicationUpdater = PartnerComplicationUpdater(applicationContext)
                partnerComplicationUpdater!!.setupComplicationUpdate()
            } else {
                Timber.i("Attempted to start partner complication updates, but updater is already running")
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
            val vitalWearApp = (applicationContext as VitalWearApp)
            val state = (vitalWearApp).partnerComplicationState
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
            // For some reason this seems to generate a lot of Job didn't exist in JobStore warnings. Seems safe to ignore at the moment.
            complicationDataSourceUpdateRequester.requestUpdateAll()
        }
    }

    fun cancel(workManager: WorkManager = WorkManager.getInstance(applicationContext)) {
        workManager.cancelAllWorkByTag(PARTNER_WORK_TAG)
    }
}