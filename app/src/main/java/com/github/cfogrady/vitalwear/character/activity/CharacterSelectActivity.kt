package com.github.cfogrady.vitalwear.character.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import com.github.cfogrady.vitalwear.settings.CharacterSettingsActivity
import com.github.cfogrady.vitalwear.util.flow.transformState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber

const val LOADING_TEXT = "Loading..."

class CharacterSelectActivity : ComponentActivity(), CharacterSelectionController {

    class NewCharacter(val cardMeta: CardMeta, val slotId: Int)

    private val vitalWearApp: VitalWearApp = application as VitalWearApp

    // used between launchers which have to be instantiated in onCreate.
    private lateinit var selectedNewCharacter : NewCharacter

    private var loadingNewCharacterFlow = MutableStateFlow(false)
    override val loadingNewCharacterState: StateFlow<Boolean> = loadingNewCharacterFlow

    private lateinit var newCharacterLauncher: ((Intent)->Unit)->Unit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityHelper = ActivityHelper(this)
        val characterManager = vitalWearApp.characterManager

        val newCharacterSettingsLauncher = activityHelper.getActivityLauncherWithResultHandling(CharacterSettingsActivity::class.java) {
            loadingNewCharacterFlow.value = true
            val settings = it.data?.getParcelableExtra(CharacterSettingsActivity.CHARACTER_SETTINGS) ?: CharacterSettings.defaultSettings()
            CoroutineScope(Dispatchers.IO).launch {
                characterManager.createNewCharacter(applicationContext, selectedNewCharacter.cardMeta, selectedNewCharacter.slotId, settings)
                finish()
            }
        }
        newCharacterLauncher = activityHelper.getActivityLauncherWithResultHandling(NewCharacterActivity::class.java) {result ->
            Timber.i("Finished from new character")
            if(newCharacterWasSelected(result)) {
                Timber.i("Received new character")
                loadingNewCharacterFlow.value = true
                val cardFromActivity = result.data?.getParcelableExtra<CardMeta>(NewCharacterActivity.CARD_SELECTED)!!
                val slotId = result.data?.getIntExtra(NewCharacterActivity.SLOT_SELECTED, 0)!!
                selectedNewCharacter = NewCharacter(cardFromActivity, slotId)
                newCharacterSettingsLauncher.invoke {
                    it.putExtra(CharacterSettingsActivity.CARD_TYPE, cardFromActivity.cardType)
                }
            }
        }
        setContent {
            CharacterSelection(this)
        }
    }

    override fun getPreviewCharacters(): List<CharacterPreview> {
        /* TODO: Changes this to just be a database call.
           Do card load into a cache for images on each individual item.
           This should keep somebody with 100+ backups from 100+ card images from having
           a ridiculously long load just to see the backups
         */
        val previewCharacterManager = vitalWearApp.previewCharacterManager
        return previewCharacterManager.previewCharacters(applicationContext).filterNot(
            CharacterPreview::isActive)
    }

    override fun newCharacter() {
        newCharacterLauncher.invoke {  }
    }

    override fun setSupportCharacter(character: CharacterPreview) {
        lifecycleScope.launch(Dispatchers.IO) {
            vitalWearApp.characterManager.setToSupport(character)
        }
    }

    override fun deleteCharacter(character: CharacterPreview) {
        vitalWearApp.characterManager.deleteCharacter(character)
    }

    private fun newCharacterWasSelected(result: ActivityResult): Boolean {
        if(result.data == null) {
            Timber.i("result has no data?")
            return false
        }
        return result.data!!.getBooleanExtra(NewCharacterActivity.NEW_CHARACTER_SELECTED_FLAG, false)
    }

    override fun swapToCharacter(character: CharacterPreview) {
        CoroutineScope(Dispatchers.IO).launch {
            vitalWearApp.characterManager.swapToCharacter(applicationContext, character)
            finish()
        }
    }

    override val vitalBoxFactory: VitalBoxFactory = vitalWearApp.vitalBoxFactory
    override val bitmapScaler: BitmapScaler = vitalWearApp.bitmapScaler
    override val supportIcon: Bitmap = vitalWearApp.firmwareManager.getFirmware().value!!.characterFirmwareSprites.supportIcon
    override val backgroundFlow: StateFlow<Bitmap> = vitalWearApp.backgroundManager.selectedBackground.transformState(vitalWearApp.backgroundManager.selectedBackground.value!!) {
        it!!
    }
}