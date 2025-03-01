package com.github.cfogrady.vitalwear.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.card.CardType
import timber.log.Timber

class CharacterSettingsActivity : ComponentActivity(), CharacterSettingsController {
    companion object {
        const val CARD_TYPE = "CardType"
        const val CHARACTER_SETTINGS = "CharacterSettings"
    }

    // Need to use getter because the application is null when first initialized.
    val vitalWearApp: VitalWearApp
        get() = application as VitalWearApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("Configure Settings")
        val cardType = intent.getSerializableExtra(CARD_TYPE) as CardType
        setContent {
            CharacterSettingsScreen(this, cardType)
        }
    }

    override suspend fun getNonDIMFranchises(): List<Int> {
        return vitalWearApp.cardMetaEntityDao.getNonDIMFranchises()
    }

    override fun finishSettings(characterSettings: CharacterSettings) {
        val intent = Intent()
        intent.putExtra(CHARACTER_SETTINGS, characterSettings)
        setResult(0, intent)
        finish()
    }
}