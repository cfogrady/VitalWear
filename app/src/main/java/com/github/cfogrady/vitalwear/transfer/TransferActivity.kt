package com.github.cfogrady.vitalwear.transfer

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

import androidx.compose.ui.unit.Dp
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.adventure.AdventureService
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.CharacterEntity
import com.github.cfogrady.vitalwear.character.data.CharacterState
import com.github.cfogrady.vitalwear.character.transformation.history.TransformationHistoryEntity
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.protos.Character
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import kotlinx.coroutines.flow.StateFlow
import java.time.LocalDateTime

class TransferActivity: ComponentActivity(), TransferScreenController {


    private val characterManager: CharacterManager
        get() = (application as VitalWearApp).characterManager
    private val adventureService: AdventureService
        get() = (application as VitalWearApp).adventureService
    private val cardMetaEntityDao: CardMetaEntityDao
        get() = (application as VitalWearApp).cardMetaEntityDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val missingPermissions = NearbyP2PConnection.getMissingPermissions(this)
        if(missingPermissions.isNotEmpty()) {
            buildPermissionRequestLauncher { requestedPermissions->
                val deniedPermissions = mutableListOf<String>()
                for(requestedPermission in requestedPermissions) {
                    if(!requestedPermission.value) {
                        deniedPermissions.add(requestedPermission.key)
                    }
                }
                if(deniedPermissions.isNotEmpty()) {
                    Toast.makeText(this, "Permission Required For Transfers", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }.launch(missingPermissions.toTypedArray())
        }
        setContent {
            TransferScreen(this)
        }
    }

    private fun buildPermissionRequestLauncher(resultBehavior: (Map<String, Boolean>)->Unit): ActivityResultLauncher<Array<String>> {
        val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
        val launcher = registerForActivityResult(multiplePermissionsContract, resultBehavior)
        return launcher
    }

    //------------------------------- Controller Members ---------------------------------------//

    override val vitalBoxFactory: VitalBoxFactory
        get() = (application as VitalWearApp).vitalBoxFactory
    override val transferBackground: Bitmap
        get() = (application as VitalWearApp).firmwareManager.getFirmware().value!!.transformationBitmaps.rayOfLightBackground
    override val bitmapScaler: BitmapScaler
        get() = (application as VitalWearApp).bitmapScaler
    override val backgroundHeight: Dp
        get() = (application as VitalWearApp).backgroundHeight

    override fun endActivityWithToast(msg: String) {
        runOnUiThread {
            Toast.makeText(this@TransferActivity, msg, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun getCharacterTransfer(): CharacterTransfer {
        return CharacterTransfer.getInstance(this)
    }

    override suspend fun getActiveCharacterProto(): Character? {
        val activeCharacter = characterManager.getCurrentCharacter()
        activeCharacter?.let {
            val maxAdventureIdxCompletedByCard = adventureService.getMaxAdventureIdxByCardCompletedForCharacter(it.characterStats.id)
            val transformationHistory = characterManager.getTransformationHistory(it.characterStats.id)
            return activeCharacter.toProto(transformationHistory, maxAdventureIdxCompletedByCard)
        }
        return null
    }

    override fun getActiveCharacter(): VBCharacter? {
        return characterManager.getCurrentCharacter()
    }

    override fun deleteActiveCharacter() {
        characterManager.deleteCurrentCharacter()
    }

    private lateinit var lastReceivedCharacter: TransferScreenController.ReceiveCharacterSprites

    override suspend fun receiveCharacter(character: Character, cardMetaEntity: CardMetaEntity) {
        val characterId = characterManager.addCharacter(
            character.cardName,
            character.characterStats.toCharacterEntity(character.cardName),
            character.settings.toCharacterSettings(),
            character.transformationHistoryList.toTransformationHistoryEntities()
        )
        adventureService.addCharacterAdventures(characterId, character.maxAdventureCompletedByCardMap)
        val happy = characterManager.getCharacterBitmap(this, character.cardName, character.characterStats.slotId, CharacterSpritesIO.WIN)
        val idle = characterManager.getCharacterBitmap(this, character.cardName, character.characterStats.slotId, CharacterSpritesIO.IDLE1)
        characterManager.swapToCharacter(this, CharacterManager.SwapCharacterIdentifier.buildAnonymous(character.cardName, characterId, character.characterStats.slotId, CharacterState.STORED))
        lastReceivedCharacter = TransferScreenController.ReceiveCharacterSprites(idle, happy)
    }

    override fun hasCard(cardName: String, cardId: Int): CardMetaEntity? {
        val cardMetaEntity = cardMetaEntityDao.getByName(cardName)
        if(cardMetaEntity != null && cardMetaEntity.cardId == cardId) {
            return cardMetaEntity
        }
        return null
    }

    override fun getLastReceivedCharacterSprites(): TransferScreenController.ReceiveCharacterSprites {
        return lastReceivedCharacter
    }

    override val cardsImported: StateFlow<Int>
        get() = TODO("Not yet implemented")

    override fun loadCards(): List<CardMetaEntity> {
        TODO("Not yet implemented")
    }
}

fun VBCharacter.toProto(transformationHistory: List<TransformationHistoryEntity>, maxAdventureCompletedByCard: Map<String, Int>): Character {
    return Character.newBuilder()
        .setCardId(this.cardMeta.cardId)
        .setCardName(this.cardName())
        .setCharacterStats(this.characterStats.toProto())
        .setSettings(this.settings.toProto())
        .addAllTransformationHistory(transformationHistory.toProtoList())
        .putAllMaxAdventureCompletedByCard(maxAdventureCompletedByCard)
        .build()
}

fun List<TransformationHistoryEntity>.toProtoList(): List<Character.TransformationEvent> {
    val transformations = mutableListOf<Character.TransformationEvent>()
    for(transformation in this) {
        transformations.add(transformation.toProto())
    }
    return transformations
}

fun TransformationHistoryEntity.toProto(): Character.TransformationEvent {
    return Character.TransformationEvent.newBuilder()
        .setCardName(this.cardName)
        .setSlotId(this.speciesId)
        .setPhase(this.phase)
        .build()
}

fun List<Character.TransformationEvent>.toTransformationHistoryEntities(): List<TransformationHistoryEntity> {
    val transformations = mutableListOf<TransformationHistoryEntity>()
    for(transformation in this) {
        transformations.add(transformation.toTransformationHistoryEntitiy())
    }
    return transformations
}

fun Character.TransformationEvent.toTransformationHistoryEntitiy(): TransformationHistoryEntity {
    return TransformationHistoryEntity(
        characterId = 0,
        phase = this.phase,
        cardName = this.cardName,
        speciesId = this.slotId
    )
}

fun CharacterEntity.toProto(): Character.CharacterStats {
    return Character.CharacterStats.newBuilder()
        .setMood(this.mood)
        .setVitals(this.vitals)
        .setInjured(this.injured)
        .setSlotId(this.slotId)
        .setTotalWins(this.totalWins)
        .setAccumulatedDailyInjuries(this.accumulatedDailyInjuries)
        .setCurrentPhaseBattles(this.currentPhaseBattles)
        .setCurrentPhaseWins(this.currentPhaseWins)
        .setTimeUntilNextTransformation(this.timeUntilNextTransformation)
        .setTotalBattles(this.totalBattles)
        .setTrainedAp(this.trainedAp)
        .setTrainedBp(this.trainedBp)
        .setTrainedHp(this.trainedHp)
        .setTrainedPp(this.trainedPP)
        .build()
}

fun CharacterSettings.toProto(): Character.Settings {
    var builder = Character.Settings.newBuilder()
        .setTrainingInBackground(this.trainInBackground)
        .setAllowedBattlesValue(this.allowedBattles.ordinal)
    if(this.assumedFranchise != null) {
        builder = builder.setAssumedFranchise(this.assumedFranchise)
    }
    return builder.build()
}

fun Character.CharacterStats.toCharacterEntity(cardName: String): CharacterEntity {
    return CharacterEntity(
        id = 0,
        state = CharacterState.STORED,
        cardFile = cardName,
        slotId = this.slotId,
        lastUpdate = LocalDateTime.now(),
        vitals = this.vitals,
        trainingTimeRemainingInSeconds = this.trainingTimeRemainingInSeconds,
        hasTransformations = this.timeUntilNextTransformation > 0,
        timeUntilNextTransformation = this.timeUntilNextTransformation,
        trainedBp = this.trainedBp,
        trainedHp = this.trainedHp,
        trainedAp = this.trainedAp,
        trainedPP = this.trainedPp,
        injured = this.injured,
        lostBattlesInjured = 0,
        accumulatedDailyInjuries = this.accumulatedDailyInjuries,
        totalBattles = this.totalBattles,
        currentPhaseBattles = this.currentPhaseBattles,
        totalWins = this.totalWins,
        currentPhaseWins = this.currentPhaseWins,
        mood = this.mood,
        sleeping = false,
        dead = false,
    )
}

fun Character.Settings.toCharacterSettings(): CharacterSettings {
    return CharacterSettings(
        characterId = 0,
        trainInBackground = this.trainingInBackground,
        allowedBattles = CharacterSettings.AllowedBattles.entries[this.allowedBattlesValue],
        if(this.hasAssumedFranchise()) this.assumedFranchise else null
    )
}