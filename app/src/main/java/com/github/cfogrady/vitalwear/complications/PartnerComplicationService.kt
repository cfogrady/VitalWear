package com.github.cfogrady.vitalwear.complications

import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Icon
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.wear.watchface.complications.data.*
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceService
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import androidx.wear.watchface.complications.datasource.ComplicationRequest
import com.github.cfogrady.vitalwear.R
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.activity.MainActivity
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PartnerComplicationService : ComplicationDataSourceService() {
    // TODO: Mood and Sleeping Sprites: 23-28
    val TAG = "PartnerComplicationService"
    lateinit var dataSource : ComponentName

    override fun getPreviewData(type: ComplicationType): ComplicationData? {
        return complicationResult()
    }

    private fun displayUninitializedFirmwareComplication() : ComplicationData {
        var iconImage = Icon.createWithResource(applicationContext, R.drawable.loading_icon)
        var image = SmallImage.Builder(iconImage, SmallImageType.PHOTO).build()
        var text = PlainComplicationText.Builder("Partner").build()
        return SmallImageComplicationData.Builder(image, text).build()
    }

    override fun onComplicationRequest(
        request: ComplicationRequest,
        listener: ComplicationRequestListener
    ) {
        Log.i(TAG, "Complication Refresh")
        dataSource = ComponentName(this, javaClass)
        val complicationData = complicationResult()
        listener.onComplicationData(complicationData)
        Handler(Looper.getMainLooper()!!).postDelayed({
            refreshComplication(request.complicationInstanceId)
        }, 500)
    }

    fun complicationResult() : ComplicationData {
        val goToAppIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, goToAppIntent, PendingIntent.FLAG_CANCEL_CURRENT.and(PendingIntent.FLAG_ONE_SHOT))
        lateinit var bitmap: Bitmap
        val characterManager = (application as VitalWearApp).characterManager
        val maybeFirmware = (application as VitalWearApp).firmwareManager.getFirmware()
        val state = (application as VitalWearApp).partnerComplicationState
        if(!characterManager.activeCharacterIsPresent()) {
            GlobalScope.launch {
                characterManager.getLiveCharacter()
            }
            if(maybeFirmware.value == null) {
                return displayUninitializedFirmwareComplication()
            }
            bitmap = maybeFirmware.value!!.loadingIcon
        } else {
            val maybeCharacter = characterManager.getLiveCharacter()
            if(maybeCharacter.value!!.characterStats.id == BEMCharacter.DEFAULT_CHARACTER.characterStats.id) {
                if(maybeFirmware.value == null) {
                    return displayUninitializedFirmwareComplication()
                }
                bitmap = maybeFirmware.value!!.insertCardIcon
            } else {
                val character = maybeCharacter.value!!
                bitmap = character.sprites.get(character.activityIdx + state.spriteIndex)
            }
        }
        var iconImage = Icon.createWithBitmap(bitmap)
        var image = SmallImage.Builder(iconImage, SmallImageType.PHOTO).build()
        var text = PlainComplicationText.Builder("Partner").build()
        return SmallImageComplicationData.Builder(image, text).setTapAction(pendingIntent).build()
    }

    fun refreshComplication(complicationId: Int) {
        //Log.i(TAG, "refreshing complication")
        val state = (application as VitalWearApp).partnerComplicationState
        state.spriteIndex++
        if(state.spriteIndex > 1) {
            state.spriteIndex = 0
        }
        val complicationDataSourceUpdateRequester =
            ComplicationDataSourceUpdateRequester.create(
                context = this,
                complicationDataSourceComponent = dataSource
            )
        complicationDataSourceUpdateRequester.requestUpdate(complicationId)
    }
}