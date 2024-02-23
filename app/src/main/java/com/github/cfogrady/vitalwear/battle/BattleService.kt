package com.github.cfogrady.vitalwear.battle

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.battle.data.*
import com.github.cfogrady.vitalwear.card.DimToBemStatConversion
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import com.github.cfogrady.vitalwear.settings.CardSettingsDao
import com.github.cfogrady.vitalwear.settings.CharacterSettings
import com.github.cfogrady.vitalwear.vitals.VitalService
import java.util.*

/**
 * This class is used to construct BattleModels for each instance of a battle
 */
class BattleService(private val cardSpritesIO: CardSpritesIO,
                    private val speciesEntityDao: SpeciesEntityDao,
                    private val characterSpritesIO: CharacterSpritesIO,
                    private val characterManager: CharacterManager,
                    private val firmwareManager: FirmwareManager,
                    private val battleLogic: BEMBattleLogic,
                    private val saveService: SaveService,
                    private val vitalService: VitalService,
                    private val backgroundManager: BackgroundManager,
                    private val random: Random,
                    private val cardSettingsDao: CardSettingsDao,
                    private val cardMetaEntityDao: CardMetaEntityDao,
                    private val dimToBemStatConversion: DimToBemStatConversion,
) {
    companion object {
        const val TAG = "BattleService"
    }

    suspend fun createBattleModel(context: Context, battleTargetInfo: BattleCharacterInfo): PreBattleModel {
        val partnerCharacter = characterManager.getCharacterFlow().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val partnerBattleCharacter = battleCharacterFromBemCharacter(context, partnerCharacter)
        val targetCard = cardMetaEntityDao.getByName(battleTargetInfo.cardName)
        val battleTarget = buildTargetFromInfo(context, partnerCharacter, targetCard, battleTargetInfo)
        val battleSpriteLoader = if(targetCard.cardType == CardType.BEM)
            BemBattleSpriteLoader(context, cardSpritesIO, targetCard.cardName, battleTargetInfo.battleBackground)
        else
            DimBattleSpriteLoader(context, firmware, cardSpritesIO, targetCard.cardName, battleTargetInfo.battleBackground)
        return PreBattleModel(
            partnerBattleCharacter,
            fetchSupportCharacter(context, firmware, partnerCharacter.getFranchise()),
            battleTarget,
            battleSpriteLoader.getBackground(),
            battleSpriteLoader.getReadyIcon(),
            battleSpriteLoader.getGoIcon(),
        )
    }

    private suspend fun fetchSupportCharacter(context: Context, firmware: Firmware, franchiseId: Int?): BattleSupportCharacter? {
        val support = characterManager.fetchSupportCharacter(context) ?: return null
        if(franchiseId != null && franchiseId != support.franchiseId) {
            Log.i(TAG, "Partner is franchise $franchiseId, but support is ${support.franchiseId}")
            return null
        }
        if(support.phase < 2) {
            Log.i(TAG, "Support isn't grown enough to support. Phase: ${support.phase}")
            return null
        }
        val spriteDir = support.spriteDirName
        return BattleSupportCharacter(
            support,
            characterSpritesIO.loadCharacterBitmapFile(context, spriteDir, CharacterSpritesIO.IDLE1)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, spriteDir, CharacterSpritesIO.ATTACK)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, spriteDir, CharacterSpritesIO.SPLASH)!!,
            getLargeAttackSprite(context, support.cardName, firmware, support.criticalAttackId)
        )
    }

    suspend fun createBattleModel(context: Context): PreBattleModel {
        val partnerCharacter = characterManager.getCharacterFlow().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val partnerBattleCharacter = battleCharacterFromBemCharacter(context, partnerCharacter)
        val randomOpponent = loadRandomTarget(context, partnerCharacter, partnerCharacter.speciesStats.phase, partnerCharacter.settings.allowedBattles, partnerCharacter.getFranchise())
        val battleSpriteLoader = if (partnerCharacter.isBEM()) BemBattleSpriteLoader(context, cardSpritesIO, partnerCharacter.cardName()) else DimBattleSpriteLoader(context, firmware, cardSpritesIO, partnerCharacter.cardName())
        val background = when(backgroundManager.battleBackgroundOption.value) {
            BackgroundManager.BattleBackgroundType.PartnerCard -> battleSpriteLoader.getBackground()
            BackgroundManager.BattleBackgroundType.Static -> backgroundManager.staticBattleBackground.value!!
            BackgroundManager.BattleBackgroundType.OpponentCard -> {
                if (randomOpponent.cardType == CardType.DIM) {
                    firmware.battleFirmwareSprites.battleBackground
                } else {
                    cardSpritesIO.loadCardBackgrounds(context, randomOpponent.cardName)[1]
                }
            }
        }
        return PreBattleModel(
            partnerBattleCharacter,
            fetchSupportCharacter(context, firmware, partnerCharacter.getFranchise()),
            randomOpponent,
            background,
            battleSpriteLoader.getReadyIcon(),
            battleSpriteLoader.getGoIcon(),
        )
    }

    private suspend fun buildTargetFromInfo(context: Context, partner: VBCharacter, opponentCardMeta: CardMetaEntity, battleTargetInfo: BattleCharacterInfo): BattleCharacter {
        var speciesEntity = speciesEntityDao.getCharacterByCardAndCharacterId(battleTargetInfo.cardName, battleTargetInfo.characterId)
        val battleStats =
        if(battleTargetInfo.hp == null || battleTargetInfo.ap == null || battleTargetInfo.bp == null || battleTargetInfo.attack == null || battleTargetInfo.critical == null) {
            if(partner.otherCardNeedsStatConversion(opponentCardMeta)) {
                speciesEntity = dimToBemStatConversion.convertSpeciesEntity(speciesEntity)
            }
            loadBattleStats(speciesEntity)
        } else {
            BattleStats(battleTargetInfo.bp, battleTargetInfo.ap, battleTargetInfo.hp, 0, speciesEntity.attribute, speciesEntity.type, speciesEntity.phase, Mood.NORMAL)
        }
        val battleSprites = loadBattleSprites(context, speciesEntity, opponentCardMeta.cardType == CardType.BEM, battleTargetInfo.attack, battleTargetInfo.critical)
        return BattleCharacter(battleTargetInfo.cardName, opponentCardMeta.cardType, battleStats, battleSprites)
    }

    fun performBattle(preBattleModel: PreBattleModel, preDeterminedHits: Array<Boolean> = emptyArray()): PostBattleModel {
        val partnerCharacter = characterManager.getCharacterFlow().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val battle = battleLogic.performBattle(preBattleModel, preDeterminedHits)
        partnerCharacter.characterStats.totalBattles++
        partnerCharacter.characterStats.currentPhaseBattles++
        if(battle.battleResult == BattleResult.WIN) {
            partnerCharacter.characterStats.totalWins++
            partnerCharacter.characterStats.currentPhaseWins++
            partnerCharacter.characterStats.mood += 10
            if(partnerCharacter.characterStats.mood > 100) {
                partnerCharacter.characterStats.mood = 100
            }
        } else {
            partnerCharacter.characterStats.mood -= 10
            if(partnerCharacter.characterStats.mood < 0) {
                partnerCharacter.characterStats.mood = 0
            }
        }
        val vitalChange = vitalService.processVitalChangeFromBattle(partnerCharacter.speciesStats.phase, preBattleModel.opponent.battleStats.stage, battle.battleResult == BattleResult.WIN)
        saveService.saveAsync()
        return PostBattleModel(
            preBattleModel.partnerCharacter,
            preBattleModel.supportCharacter,
            preBattleModel.opponent,
            battle,
            preBattleModel.background,
            firmware.battleFirmwareSprites.partnerHpIcons,
            firmware.battleFirmwareSprites.opponentHpIcons,
            vitalChange
        )
    }

    private fun loadBattleCharacter(context: Context, cardName: String, cardType: CardType, speciesEntity: SpeciesEntity, hasCardHits: Boolean): BattleCharacter {
        val battleStats = loadBattleStats(speciesEntity)
        val battleSprites = loadBattleSprites(context, speciesEntity, hasCardHits)
        return BattleCharacter(cardName, cardType, battleStats, battleSprites)
    }

    private fun loadBattleStats(speciesEntity: SpeciesEntity, mood: Mood = Mood.NORMAL): BattleStats {
        return BattleStats(speciesEntity.bp, speciesEntity.ap, speciesEntity.hp, 0, speciesEntity.attribute, speciesEntity.type, speciesEntity.phase, mood)
    }

    private fun loadBattleSprites(context: Context, speciesEntity: SpeciesEntity, hasCardHits: Boolean, attack: Int? = null, critical: Int? = null): BattleSprites {
        val firmware = firmwareManager.getFirmware().value!!
        val smallAttackId = attack ?: speciesEntity.attackId
        val largeAttackId = critical ?: speciesEntity.criticalAttackId
        val projectileSprite = getSmallAttackSprite(context, speciesEntity.cardName, firmware, smallAttackId)
        val largeProjectileSprite = getLargeAttackSprite(context, speciesEntity.cardName, firmware, largeAttackId)
        return BattleSprites(
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.NAME)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.IDLE1, resize = true)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.ATTACK, resize = true)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.DODGE, resize = true)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.WIN, resize = true)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.DOWN, resize = true)!!,
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.SPLASH)!!,
            projectileSprite,
            largeProjectileSprite,
            getHitSprite(context, speciesEntity.cardName, hasCardHits, firmware)
        )
    }

    private fun battleCharacterFromBemCharacter(context: Context, character: VBCharacter): BattleCharacter {
        val firmware = firmwareManager.getFirmware().value!!
        val smallAttackId = character.speciesStats.attackId
        val largeAttackId = character.speciesStats.criticalAttackId
        val projectileSprite = getSmallAttackSprite(context, character.cardName(), firmware, smallAttackId)
        val largeProjectileSprite = getLargeAttackSprite(context, character.cardName(), firmware, largeAttackId)
        val battleStats = BattleStats(
            character.totalBp(),
            character.totalAp(),
            character.totalHp(),
            character.characterStats.vitals,
            character.speciesStats.attribute,
            character.speciesStats.type,
            character.speciesStats.phase,
            character.mood(),
        )
        val battleSprites = BattleSprites(
            character.characterSprites.sprites[CharacterSprites.NAME],
            character.characterSprites.sprites[CharacterSprites.IDLE_1],
            character.characterSprites.sprites[CharacterSprites.ATTACK],
            character.characterSprites.sprites[CharacterSprites.DODGE],
            character.characterSprites.sprites[CharacterSprites.WIN],
            character.characterSprites.sprites[CharacterSprites.DOWN],
            character.characterSprites.sprites[CharacterSprites.SPLASH],
            projectileSprite,
            largeProjectileSprite,
            getHitSprite(context, character.cardName(), character.isBEM(), firmware)
        )
        return BattleCharacter(character.cardName(), character.cardMeta.cardType, battleStats, battleSprites)
    }

    private fun getSmallAttackSprite(context: Context, cardName: String, firmware: Firmware, smallAttackId: Int) : Bitmap {
        if(smallAttackId <= 38) {
            return firmware.battleFirmwareSprites.attackSprites[smallAttackId]
        }
        val cardSpriteIdx = (smallAttackId - 39) + 34
        return cardSpritesIO.loadIndexedSprite(context, cardName, CardSpritesIO.ATTACKS, cardSpriteIdx)
    }

    private fun getLargeAttackSprite(context: Context, cardName: String, firmware: Firmware, largeAttackId: Int) : Bitmap {
        if(largeAttackId < 22) {
            return firmware.battleFirmwareSprites.largeAttackSprites[largeAttackId]
        }
        val cardSpriteIdx = (largeAttackId - 22) + 44
        return cardSpritesIO.loadIndexedSprite(context, cardName, CardSpritesIO.CRITICAL_ATTACKS, cardSpriteIdx)
    }

    private fun getHitSprite(context: Context, cardName: String, hasCardHits: Boolean, firmware: Firmware): List<Bitmap> {
        if(hasCardHits) {
            return cardSpritesIO.loadCardSpriteList(context, cardName, CardSpritesIO.RECEIVE_HIT_ICONS)
        }
        return firmware.battleFirmwareSprites.hitIcons
    }

    private suspend fun loadRandomTarget(context: Context, partner: VBCharacter, phase: Int, allowedBattles: CharacterSettings.AllowedBattles, franchiseId: Int): BattleCharacter {
        val targetCard = when (allowedBattles) {
            CharacterSettings.AllowedBattles.ALL -> {
                val options = cardSettingsDao.getAllBattleCards()
                options[random.nextInt(options.size)]
            }
            CharacterSettings.AllowedBattles.ALL_FRANCHISE_AND_DIM -> {
                val franchiseOptions = cardSettingsDao.getAllFranchiseBattleCards(franchiseId)
                val dimOptions = cardSettingsDao.getAllFranchiseBattleCards(0)
                val options = ArrayList<CardMetaEntity>(franchiseOptions.size + dimOptions.size)
                options.addAll(franchiseOptions)
                options.addAll(dimOptions)
                options[random.nextInt(options.size)]
            }
            CharacterSettings.AllowedBattles.ALL_FRANCHISE -> {
                val options = cardSettingsDao.getAllFranchiseBattleCards(franchiseId)
                options[random.nextInt(options.size)]
            }
            CharacterSettings.AllowedBattles.CARD_ONLY -> {
                partner.cardMeta.toCardMetaEntity()
            }
        }
        var speciesEntity = assignRandomTargetFromCard(targetCard.cardName, targetCard.cardType == CardType.BEM, phase)
        if(partner.otherCardNeedsStatConversion(targetCard)) {
            speciesEntity = dimToBemStatConversion.convertSpeciesEntity(speciesEntity)
        }
        val hasCardHits = targetCard.cardType == CardType.BEM
        return loadBattleCharacter(context, targetCard.cardName, targetCard.cardType, speciesEntity, hasCardHits)
    }

    private fun assignRandomTargetFromCard(cardName: String, hasThirdBattlePool: Boolean, activeStage: Int): SpeciesEntity {
        var roll = random.nextInt(100)
        val cardSpecies = speciesEntityDao.getCharacterByCard(cardName)
        do {
            for(species in cardSpecies) {
                if(activeStage < 4) {
                    if(species.battlePool1 != DimReader.NONE_VALUE) {
                        roll -= species.battlePool1
                    }
                } else if(activeStage < 6 || !hasThirdBattlePool) {
                    if(species.battlePool2 != DimReader.NONE_VALUE) {
                        roll -= species.battlePool2
                    }
                } else {
                    if(species.battlePool3 != DimReader.NONE_VALUE) {
                        roll -= species.battlePool3
                    }
                }
                if(roll < 0) {
                    return species
                }
            }
            Log.w(
                BattleActivity.TAG, "Rolled for an invalid species... This is a bad card image." +
                        "Running through the options until our roll is reduced to 0.")
        } while (true) // always true because if it were false we would have already returned
    }
}