package com.github.cfogrady.vitalwear.complications

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

class ComplicationRefreshService(private val applicationContext: Context, private val complicationState: PartnerComplicationState) {
    fun refreshVitalsComplication() {
        val component = ComponentName(applicationContext, VitalsComplicationService::class.java)
        val complicationDataSourceUpdateRequester =
            ComplicationDataSourceUpdateRequester.create(
                context = applicationContext,
                complicationDataSourceComponent = component
            )
        for(complicationId in complicationState.vitalComplicationIds) {
            complicationDataSourceUpdateRequester.requestUpdate(complicationId)
        }
    }
}