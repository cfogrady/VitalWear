package com.github.cfogrady.vitalwear

import android.content.ComponentName
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.github.cfogrady.vitalwear.character.data.Character


class PartnerComplicationService : ComplicationDataSourceService() {
    // TODO: Mood and Sleeping Sprites: 23-28
    val TAG = "PartnerComplicationService"
    lateinit var dataSource : ComponentName

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        var currentPartner = (application as VitalWearApp).characterManager.activePartner
        var iconImage = Icon.createWithBitmap(currentPartner.sprites.get(currentPartner.spriteIdx))
        var image = SmallImage.Builder(iconImage, SmallImageType.PHOTO).build()
        var text = PlainComplicationText.Builder("Partner").build()
        return SmallImageComplicationData.Builder(image, text).build()
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        dataSource = ComponentName(this, javaClass)
        var currentPartner = (application as VitalWearApp).characterManager.activePartner
        var iconImage = Icon.createWithBitmap(currentPartner.sprites.get(currentPartner.spriteIdx))
        var image = SmallImage.Builder(iconImage, SmallImageType.ICON).build()
        var text = PlainComplicationText.Builder("Partner").build()
        listener.onComplicationData(SmallImageComplicationData.Builder(image, text).build())
        Handler(Looper.getMainLooper()!!).postDelayed({
            refreshComplication(currentPartner, request.complicationInstanceId)
        }, 500)
    }

    fun refreshComplication(character: Character, complicationId: Int) {
        //Log.i(TAG, "refreshing complication")
        character.spriteIdx++
        if(character.spriteIdx > 2) {
            character.spriteIdx = 1
        }
        val complicationDataSourceUpdateRequester =
            ComplicationDataSourceUpdateRequester.create(
                context = this,
                complicationDataSourceComponent = dataSource
            )
        complicationDataSourceUpdateRequester.requestUpdate(complicationId)
    }
}