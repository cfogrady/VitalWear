package com.github.cfogrady.vitalwear.adventure

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.adventure.firmware.AdventureFirmwareSprites
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.AdventureEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.settings.CharacterSettings

class AdventureMenuActivity : ComponentActivity(), AdventureMenuScreenController {

    // need to use getters because application is during construction
    val vitalWearApp: VitalWearApp
        get() = application as VitalWearApp

    override val firmware: Firmware
        get() = vitalWearApp.firmwareManager.getFirmware().value!!
    override val backgroundHeight: Dp
        get() = vitalWearApp.backgroundHeight
    override val characterSprites: CharacterSprites
        get() = vitalWearApp.characterManager.getCurrentCharacter()!!.characterSprites
    override val adventureFirmwareSprites: AdventureFirmwareSprites
        get() = firmware.adventureFirmwareSprites
    override val bitmapScaler: BitmapScaler
        get() = vitalWearApp.bitmapScaler
    override val vitalBoxFactory: VitalBoxFactory
        get() = vitalWearApp.vitalBoxFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AdventureMenuScreen(this) {
                finish()
            }
        }
    }

    override fun loadCardBackgrounds(cardName: String): List<Bitmap> {
        return vitalWearApp.cardSpriteIO.loadCardBackgrounds(this, cardName)
    }

    override fun loadCardIcon(cardName: String): Bitmap {
        return vitalWearApp.cardSpriteIO.loadCardSprite(this, cardName, CardSpritesIO.ICON)
    }

    override fun startAdventure(cardName: String, selectedAdventureId: Int) {
        val foregroundIntent = Intent(this, AdventureForegroundService::class.java)
        foregroundIntent.putExtra(AdventureForegroundService.CARD_NAME, cardName)
        foregroundIntent.putExtra(AdventureForegroundService.STARTING_ADVENTURE, selectedAdventureId)
        startForegroundService(foregroundIntent)
    }

    override fun loadCardsForActiveCharacterFranchise(): List<CardMetaEntity> {
        val activeCharacter = vitalWearApp.characterManager.getCurrentCharacter()!!
        val characterSettings = activeCharacter.settings
        val franchise = activeCharacter.getFranchise()
        return when(characterSettings.allowedBattles) {
            CharacterSettings.AllowedBattles.ALL_FRANCHISE_AND_DIM -> {
                vitalWearApp.cardMetaEntityDao.getByFranchiseIn(listOf(franchise, CardMeta.DIM_FRANCHISE))
            }
            CharacterSettings.AllowedBattles.ALL -> {
                vitalWearApp.cardMetaEntityDao.getAll()
            }
            else -> vitalWearApp.cardMetaEntityDao.getByFranchise(franchise)
        }
    }

    override suspend fun getMaxAdventureForActiveCharacter(cardName: String): Int {
        val activeCharacter = vitalWearApp.characterManager.getCurrentCharacter()!!
        return vitalWearApp.adventureService.getCurrentMaxAdventure(activeCharacter.characterStats.id, cardName)
    }

    override suspend fun getAdventuresForCard(cardName: String): List<AdventureEntity> {
        return vitalWearApp.adventureService.getAdventureOptions(cardName)
    }

    override suspend fun loadBossCharacterBitmap(cardName: String, characterId: Int): Bitmap {
        val bossSpecies = vitalWearApp.database.speciesEntityDao().getCharacterByCardAndCharacterId(cardName, characterId)
        return vitalWearApp.characterSpritesIO.loadCharacterBitmapFile(this, bossSpecies.spriteDirName, CharacterSpritesIO.IDLE1)!!
    }

}