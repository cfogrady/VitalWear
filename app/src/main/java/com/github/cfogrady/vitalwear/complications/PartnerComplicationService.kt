package com.github.cfogrady.vitalwear.complications

import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Icon
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.MainActivity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites

class PartnerComplicationService : ComplicationDataSourceService() {

    // TODO: Mood and Sleeping Sprites: 23-28
    val TAG = "PartnerComplicationService"

    // Called when setting up complication (edit screens)
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return complicationResult()
    }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        super.onComplicationActivated(complicationInstanceId, type)
        val complicationRefreshService = (application as VitalWearApp).complicationRefreshService
        complicationRefreshService.startupPartnerComplications()
    }

    // Called when running and refresh is requested
    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        request.complicationInstanceId
        val complicationData = complicationResult()
        listener.onComplicationData(complicationData)
    }

    override fun onComplicationDeactivated(complicationInstanceId: Int) {
        super.onComplicationDeactivated(complicationInstanceId)
    }

    private fun complicationResult() : ComplicationData {
        val goToAppIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, goToAppIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
                .or(PendingIntent.FLAG_IMMUTABLE)
                .or(PendingIntent.FLAG_ONE_SHOT))
        val iconImage = findComplicationIcon()
        val image = SmallImage.Builder(iconImage, SmallImageType.PHOTO).build()
        val text = PlainComplicationText.Builder("Partner").build()
        return SmallImageComplicationData.Builder(image, text).setTapAction(pendingIntent).build()
    }

    private fun findComplicationIcon(): Icon {
        val characterManager = (application as VitalWearApp).characterManager
        val maybeFirmware = (application as VitalWearApp).firmwareManager.getFirmware()
        val state = (application as VitalWearApp).partnerComplicationState
        val character = characterManager.getCurrentCharacter()
        return if(maybeFirmware.value == null || !characterManager.initialized.value) {
            Icon.createWithResource(applicationContext, com.github.cfogrady.vitalwear.common.R.drawable.loading_icon)
        } else if(character == null) {
            Icon.createWithBitmap(maybeFirmware.value!!.insertCardIcon)
        } else if (character.characterStats.sleeping) {
            val sleepingBitmap = character.characterSprites.sprites[CharacterSprites.DOWN]
            Icon.createWithBitmap(sleepingBitmap)
        } else {
            val characterBitmaps = (application as VitalWearApp).gameState.value.bitmaps(character)
            Icon.createWithBitmap(characterBitmaps[state.spriteIndex])
        }
    }
}