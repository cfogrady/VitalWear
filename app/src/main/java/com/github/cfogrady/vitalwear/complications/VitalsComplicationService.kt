package com.github.cfogrady.vitalwear.complications

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.MainActivity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter

class VitalsComplicationService : ComplicationDataSourceService() {
    // TODO: Merge common logic with PartnerComplicationService into shared dependency
    val TAG = "PartnerComplicationService"

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return complicationResult()
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        //TODO: Use this to remove deactivated complications
        super.onComplicationDeactivated(complicationInstanceId)
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        (application as VitalWearApp).partnerComplicationState.vitalComplicationIds.add(request.complicationInstanceId)
        val complicationData = complicationResult()
        listener.onComplicationData(complicationData)
    }

    private fun complicationResult() : ComplicationData {
        val showCharacterIcon = false
        val goToAppIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, goToAppIntent, PendingIntent.FLAG_CANCEL_CURRENT.and(
            PendingIntent.FLAG_ONE_SHOT))
        val characterManager = (application as VitalWearApp).characterManager
        var vitals = 0
        var iconImage: Icon? = null
        if(characterManager.initialized.value!! && characterManager.activeCharacterIsPresent()) {
            val character = characterManager.getCurrentCharacter()
            vitals = character.characterStats.vitals
            if(character != BEMCharacter.DEFAULT_CHARACTER && showCharacterIcon) {
                iconImage = Icon.createWithBitmap(character.sprites[1])
            }
        }
        var descr = PlainComplicationText.Builder("VITALS").build()
        var valueText = PlainComplicationText.Builder("$vitals").build()
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