package com.github.cfogrady.vitalwear

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.github.cfogrady.vitalwear.data.CardLoader
import com.github.cfogrady.vitalwear.data.FirmwareManager

class BackgroundManager(private val cardLoader: CardLoader, private val firmwareManager: FirmwareManager) {
    val TAG = "BackgroundManager"
    val selectedBackground = MutableLiveData<Bitmap>()

    fun loadDefault() {
        val liveFirmware = firmwareManager.getFirmware()
        if(liveFirmware.value != null) {
            selectedBackground.postValue(liveFirmware.value!!.defaultBackground)
        } else {
            Log.w(TAG, "Somehow trying to load the background before firmware is loaded.")
        }
    }
}