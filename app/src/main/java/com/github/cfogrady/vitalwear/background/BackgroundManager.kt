package com.github.cfogrady.vitalwear.background

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class BackgroundManager(private val cardSpritesIO: CardSpritesIO, private val sharedPreferences: SharedPreferences) {

    companion object {
        const val BACKGROUND_IS_CARD = "BACKGROUND_IS_CARD"
        const val BACKGROUND_CARD_NAME = "BACKGROUND_CARD_NAME"
        const val BACKGROUND_IDX = "BACKGROUND_IDX"
        const val BATTLE_BACKGROUND_OPTION = "BATTLE_BACKGROUND_OPTION"
        const val STATIC_BATTLE_BACKGROUND_IS_CARD = "STATIC_BATTLE_BACKGROUND_IS_CARD"
        const val STATIC_BATTLE_BACKGROUND_CARD_NAME = "STATIC_BATTLE_BACKGROUND_CARD_NAME"
        const val STATIC_BATTLE_BACKGROUND_IDX = "STATIC_BATTLE_BACKGROUND_IDX"
    }

    enum class BackgroundType(val isCardKey: String, val cardNameKey: String, val idxKey: String) {
        Normal(BACKGROUND_IS_CARD, BACKGROUND_CARD_NAME, BACKGROUND_IDX),
        Battle(STATIC_BATTLE_BACKGROUND_IS_CARD, STATIC_BATTLE_BACKGROUND_CARD_NAME, STATIC_BATTLE_BACKGROUND_IDX)
    }

    enum class BattleBackgroundType {
        OpponentCard,
        PartnerCard,
        Static,
    }

    private val _selectedBackground = MutableStateFlow<Bitmap?>(null)
    val selectedBackground: StateFlow<Bitmap?> = _selectedBackground
    private val _battleBackgroundOption = MutableStateFlow(BattleBackgroundType.PartnerCard)
    val battleBackgroundOption: StateFlow<BattleBackgroundType> = _battleBackgroundOption
    private val _staticBattleBackground = MutableStateFlow<Bitmap?>(null)
    val staticBattleBackground: StateFlow<Bitmap?> = _staticBattleBackground
    lateinit var firmware: Firmware

    fun loadBackgrounds(context: Context, firmware: Firmware) {
        this.firmware = firmware
        loadBackground(context, BACKGROUND_IS_CARD, BACKGROUND_IDX, BACKGROUND_CARD_NAME) {
            _selectedBackground.value = it
        }
        _battleBackgroundOption.value = BattleBackgroundType.valueOf(sharedPreferences.getString(BATTLE_BACKGROUND_OPTION, BattleBackgroundType.PartnerCard.name)!!)
        if(_battleBackgroundOption.value == BattleBackgroundType.Static) {
            loadBackground(context, STATIC_BATTLE_BACKGROUND_IS_CARD, STATIC_BATTLE_BACKGROUND_IDX, STATIC_BATTLE_BACKGROUND_CARD_NAME) {
                _staticBattleBackground.value = it
            }
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
                Timber.e("Attempting to load card background, but card background name is null!")
            }
        }
    }

    fun setFirmwareBackground(backgroundType: BackgroundType, index: Int) {
        val preferences = sharedPreferences.edit()
        if (index < 4) {
            preferences.putBoolean(backgroundType.isCardKey, false)
                .putInt(backgroundType.idxKey, index)
            val background = firmware.backgrounds[index]
            when(backgroundType) {
                BackgroundType.Normal -> _selectedBackground.value = background
                BackgroundType.Battle -> {
                    _staticBattleBackground.value = background
                    _battleBackgroundOption.value = BattleBackgroundType.Static
                    preferences.putString(BATTLE_BACKGROUND_OPTION, BattleBackgroundType.Static.name)
                }
            }
            preferences.apply()
        } else {
            Timber.e("Received invalid firmware background: $index out of 4")
        }
    }

    fun setBattleBackgroundPartner() {
        _battleBackgroundOption.value = BattleBackgroundType.PartnerCard
    }

    fun setBattleBackgroundOpponent() {
        _battleBackgroundOption.value = BattleBackgroundType.OpponentCard
    }

    fun setCardBackground(backgroundType: BackgroundType, cardName: String, index: Int, bitmap: Bitmap) {
        val preferences = sharedPreferences.edit()
        preferences.putBoolean(backgroundType.isCardKey, true)
            .putString(backgroundType.cardNameKey, cardName)
            .putInt(backgroundType.idxKey, index)
        when(backgroundType) {
            BackgroundType.Normal -> _selectedBackground.value = bitmap
            BackgroundType.Battle -> {
                _staticBattleBackground.value = bitmap
                _battleBackgroundOption.value = BattleBackgroundType.Static
                preferences.putString(BATTLE_BACKGROUND_OPTION, BattleBackgroundType.Static.name)
            }
        }
        preferences.apply()

    }
}