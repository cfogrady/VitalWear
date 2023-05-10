package com.github.cfogrady.vitalwear.complications

import android.content.ComponentName
import android.content.Context
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester

class ComplicationRefreshService(private val complicationState: PartnerComplicationState) {
    fun refreshVitalsComplication(context: Context) {
        val component = ComponentName(context.applicationContext, VitalsComplicationService::class.java)
        val complicationDataSourceUpdateRequester =
            ComplicationDataSourceUpdateRequester.create(
                context = context,
                complicationDataSourceComponent = component
            )
        for(complicationId in complicationState.vitalComplicationIds) {
            complicationDataSourceUpdateRequester.requestUpdate(complicationId)
        }
    }
}