package com.github.cfogrady.vitalwear.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.main.MainActivity

class VitalsComplicationService : ComplicationDataSourceService() {
    // TODO: Merge common logic with PartnerComplicationService into shared dependency
    val TAG = "PartnerComplicationService"

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return complicationResult()
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        val complicationData = complicationResult()
        listener.onComplicationData(complicationData)
    }

    private fun complicationResult() : ComplicationData {
        val goToAppIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, goToAppIntent, PendingIntent.FLAG_CANCEL_CURRENT.or(
            PendingIntent.FLAG_ONE_SHOT).or(PendingIntent.FLAG_IMMUTABLE))
        val characterManager = (application as VitalWearApp).characterManager
        var vitals = 0
        var iconImage: Icon? = null
        val currentCharacter = characterManager.getCurrentCharacter()
        if(characterManager.initialized.value && currentCharacter != null) {
            vitals = currentCharacter.characterStats.vitals
        }
        val firmwareManager = (application as VitalWearApp).firmwareManager
        if(firmwareManager.getFirmware().value != null) {
            val firmware = firmwareManager.getFirmware().value!!
            iconImage = Icon.createWithBitmap(firmware.characterIconBitmaps.vitalsIcon)
        }
        val descr = PlainComplicationText.Builder("VITALS").build()
        val valueText = PlainComplicationText.Builder("$vitals").build()
        val resultDataBuilder = RangedValueComplicationData.Builder(value = vitals.toFloat(), min = 0f, max = 9999f, descr)
            .setText(valueText)
            .setTapAction(pendingIntent)
        if(iconImage != null) {
            val img = MonochromaticImage.Builder(iconImage).build()
            resultDataBuilder.setMonochromaticImage(img)
        } else {
            resultDataBuilder.setTitle(descr)
        }
        return resultDataBuilder.build()
    }
}