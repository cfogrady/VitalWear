package com.github.cfogrady.vitalwear.complications

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.MainActivity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter


class PartnerComplicationService : ComplicationDataSourceService() {
    // TODO: Mood and Sleeping Sprites: 23-28
    val TAG = "PartnerComplicationService"
    lateinit var dataSource : ComponentName

    // Called when setting up complication (edit screens)
    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return complicationResult()
    }

    override fun onComplicationActivated(complicationInstanceId: Int, type: ComplicationType) {
        super.onComplicationActivated(complicationInstanceId, type)
        val complicationRefreshService = (application as VitalWearApp).complicationRefreshService
        complicationRefreshService.startupPartnerComplications()
    }

    private fun displayUninitializedFirmwareComplication() : ComplicationData {
        val iconImage = Icon.createWithResource(applicationContext, R.drawable.loading_icon)
        val image = SmallImage.Builder(iconImage, SmallImageType.PHOTO).build()
        val text = PlainComplicationText.Builder("Partner").build()
        return SmallImageComplicationData.Builder(image, text).build()
    }

    // Called when running and refresh is requested
    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        dataSource = ComponentName(this, javaClass)
        val complicationData = complicationResult()
        listener.onComplicationData(complicationData)
    }

    private fun complicationResult() : ComplicationData {
        val goToAppIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, goToAppIntent, PendingIntent.FLAG_CANCEL_CURRENT.and(PendingIntent.FLAG_ONE_SHOT))
        lateinit var bitmap: Bitmap
        val characterManager = (application as VitalWearApp).characterManager
        val maybeFirmware = (application as VitalWearApp).firmwareManager.getFirmware()
        val state = (application as VitalWearApp).partnerComplicationState
        if(!characterManager.initialized.value!! || !characterManager.activeCharacterIsPresent()) {
            if(maybeFirmware.value == null) {
                return displayUninitializedFirmwareComplication()
            }
            bitmap = maybeFirmware.value!!.loadingIcon
        } else {
            val character = characterManager.getCurrentCharacter()
            bitmap = if(character == BEMCharacter.DEFAULT_CHARACTER) {
                if(maybeFirmware.value == null) {
                    return displayUninitializedFirmwareComplication()
                }
                maybeFirmware.value!!.insertCardIcon
            } else {
                character.characterSprites.sprites[character.activityIdx + state.spriteIndex]
            }
        }
        val iconImage = Icon.createWithBitmap(bitmap)
        val image = SmallImage.Builder(iconImage, SmallImageType.PHOTO).build()
        val text = PlainComplicationText.Builder("Partner").build()
        return SmallImageComplicationData.Builder(image, text).setTapAction(pendingIntent).build()
    }
}