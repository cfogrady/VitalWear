package com.github.cfogrady.vitalwear.battle

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vitalwear.SaveService
import com.github.cfogrady.vitalwear.battle.data.*
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.card.CardSpritesIO
import com.github.cfogrady.vitalwear.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import com.github.cfogrady.vitalwear.character.data.CharacterSprites
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
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
                    private val random: Random,
) {
    companion object {
    }

    fun createBattleModel(context: Context): PreBattleModel {
        val partnerCharacter = characterManager.getLiveCharacter().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val partnerBattleCharacter = battleCharacterFromBemCharacter(context, partnerCharacter)
        val hasCardHits = partnerCharacter.isBEM()
        val hasThirdBattlePool = partnerCharacter.isBEM()
        val randomOpponent = loadRandomTarget(context, partnerCharacter.cardName(), hasThirdBattlePool, hasCardHits, partnerCharacter.speciesStats.phase)
        val battleSpriteLoader = if (partnerCharacter.isBEM()) CardBattleSpriteLoader(context, cardSpritesIO, partnerCharacter.cardName()) else FirmwareBattleSpriteLoader(firmware)
        return PreBattleModel(
            partnerBattleCharacter,
            randomOpponent,
            battleSpriteLoader.getBackground(),
            battleSpriteLoader.getReadyIcon(),
            battleSpriteLoader.getGoIcon(),
        )
    }

    fun performBattle(preBattleModel: PreBattleModel): PostBattleModel {
        val partnerCharacter = characterManager.getLiveCharacter().value!!
        val firmware = firmwareManager.getFirmware().value!!
        val battle = battleLogic.performBattle(preBattleModel)
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
            preBattleModel.opponent,
            battle,
            preBattleModel.background,
            firmware.battleFirmwareSprites.partnerHpIcons,
            firmware.battleFirmwareSprites.opponentHpIcons,
            vitalChange
        )
    }

    private fun loadBattleCharacter(context: Context, speciesEntity: SpeciesEntity, hasCardHits: Boolean): BattleCharacter {
        val battleStats = loadBattleStats(speciesEntity)
        val battleSprites = loadBattleSprites(context, speciesEntity, hasCardHits)
        return BattleCharacter(battleStats, battleSprites)
    }

    private fun loadBattleStats(speciesEntity: SpeciesEntity, mood: Mood = Mood.NORMAL): BattleStats {
        return BattleStats(speciesEntity.bp, speciesEntity.ap, speciesEntity.hp, 0, speciesEntity.attribute, speciesEntity.type, speciesEntity.phase, mood)
    }

    private fun loadBattleSprites(context: Context, speciesEntity: SpeciesEntity, hasCardHits: Boolean): BattleSprites {
        val firmware = firmwareManager.getFirmware().value!!
        val smallAttackId = speciesEntity.attackId
        val largeAttackId = speciesEntity.criticalAttackId
        val projectileSprite = getSmallAttackSprite(context, speciesEntity.cardName, firmware, smallAttackId)
        val largeProjectileSprite = getLargeAttackSprite(context, speciesEntity.cardName, firmware, largeAttackId)
        return BattleSprites(
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.NAME),
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.IDLE1),
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.ATTACK),
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.DODGE),
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.WIN),
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.DOWN),
            characterSpritesIO.loadCharacterBitmapFile(context, speciesEntity.spriteDirName, CharacterSpritesIO.SPLASH),
            projectileSprite,
            largeProjectileSprite,
            getHitSprite(context, speciesEntity.cardName, hasCardHits, firmware)
        )
    }

    private fun battleCharacterFromBemCharacter(context: Context, character: BEMCharacter): BattleCharacter {
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
        return BattleCharacter(battleStats, battleSprites)
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

    private fun loadRandomTarget(context: Context, cardName: String, hasThirdBattlePool: Boolean, hasCardHits: Boolean, phase: Int): BattleCharacter {
        val speciesEntity = assignRandomTargetFromCard(cardName, hasThirdBattlePool, phase)
        return loadBattleCharacter(context, speciesEntity, hasCardHits)
    }

    private fun assignRandomTargetFromCard(cardName: String, hasThirdBattlePool: Boolean, activeStage: Int): SpeciesEntity {
        var roll = random.nextInt(100)
        val cardSpecies = speciesEntityDao.getCharacterByCard(cardName)
        while(roll >= 0) {
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
        }
        throw java.lang.IllegalStateException("If we get here then we somehow failed to return the species that brought out role under 0.")
    }
}