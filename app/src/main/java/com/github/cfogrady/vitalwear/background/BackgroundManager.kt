package com.github.cfogrady.vitalwear.background

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BackgroundManager(private val cardSpritesIO: CardSpritesIO, private val sharedPreferences: SharedPreferences) {

    companion object {
        const val TAG = "BackgroundManager"
        const val IS_CARD_BACKGROUND = "IS_CARD_BACKGROUND"
        const val CARD_BACKGROUND_NAME = "CARD_BACKGROUND_NAME"
        const val BACKGROUND_IDX = "BACKGROUND_IDX"
    }

    private val _selectedBackground = MutableStateFlow<Bitmap?>(null)
    val selectedBackground: StateFlow<Bitmap?> = _selectedBackground
    lateinit var firmware: Firmware

    fun loadBackgrounds(context: Context, firmware: Firmware) {
        this.firmware = firmware
        loadBackground(context, IS_CARD_BACKGROUND, BACKGROUND_IDX, CARD_BACKGROUND_NAME) {
            _selectedBackground.value = it
        }
    }

    private fun loadBackground(context: Context, isCardStringKey: String, indexKey: String, cardNameKey: String, backgroundSetter: (Bitmap) -> Unit) {
        val cardBackground = sharedPreferences.getBoolean(isCardStringKey, false)
        val backgroundIdx = sharedPreferences.getInt(indexKey, 0)
        if(!cardBackground) {
            backgroundSetter.invoke(firmware.backgrounds[backgroundIdx])
        } else {
            val cardName = sharedPreferences.getString(cardNameKey, null)
            if(cardName != null) {
                val backgrounds = cardSpritesIO.loadCardBackgrounds(context, cardName)
                backgroundSetter.invoke(backgrounds[backgroundIdx])
            } else {
                Log.e(TAG, "Attempting to load card background, but card background name is null!")
            }
        }
    }

    fun setFirmwareBackground(index: Int) {
        if (index < 4) {
            _selectedBackground.value = firmware.backgrounds[index]
            sharedPreferences.edit().putBoolean(IS_CARD_BACKGROUND, false)
                .putInt(BACKGROUND_IDX, index)
                .apply()
        } else {
            Log.e(TAG, "Received invalid firmware background: $index out of 4")
        }
    }

    fun setCardBackground(cardName: String, index: Int, context: Context) {
        val backgrounds = cardSpritesIO.loadCardBackgrounds(context, cardName)
        setCardBackground(cardName, index, backgrounds[index])
    }

    fun setCardBackground(cardName: String, index: Int, bitmap: Bitmap) {
        _selectedBackground.value = bitmap
        sharedPreferences.edit().putBoolean(IS_CARD_BACKGROUND, true)
            .putString(CARD_BACKGROUND_NAME, cardName)
            .putInt(BACKGROUND_IDX, index)
            .apply()
    }
}