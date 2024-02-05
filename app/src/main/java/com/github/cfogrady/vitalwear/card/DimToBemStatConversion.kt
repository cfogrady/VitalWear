package com.github.cfogrady.vitalwear.card

import android.util.Log
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.max

class DimToBemStatConversion(private val statConversionDao: StatConversionDao) {
    companion object {
        const val TAG = "DimToBemStatConversion"

        val MIN_DIM_STATS = listOf(
            Stats(0f, 0f, 0f), // I
            Stats(0f,0f,0f), // II
            Stats(10f,3f,2f), // III
            Stats(15f,4f,2f), // IV
            Stats(25f,6f,3f), // V
            Stats(45f,10f,5f), // VI
        )

        val AVERAGE_DIM_STATS = listOf(
            Stats(0f, 0f, 0f), // I
            Stats(0f,0f,0f), // II
            Stats(10f,3f,2f), // III
            Stats(20.2727f,4.5182f,2.5727f), // IV
            Stats(32.9221f,9.6494f,4.0519f), // V
            Stats(52.4771f,13.5963f,7.1743f), // VI
            )

        val MAX_DIM_STATS = listOf(
            Stats(0f, 0f, 0f), // I
            Stats(0f,0f,0f), // II
            Stats(10f,3f,2f), // III
            Stats(25f,6f,3f), // IV
            Stats(40f,12f,6f), // V
            Stats(70f,22f,12f), // VI
        )

        val STDDEV_BEM_STATS = listOf(
            Stats(0f, 0f,0f), // I
            Stats(0f, 0f,0f), // II
            Stats(421.3150f, 249.2136f,72.4568f), // III
            Stats(470.1532f, 213.6744f,66.5749f), // IV
            Stats(616.0121f, 367.4271f,89.2335f), // V
            Stats(679.0475f, 429.7718f,98.3021f), // VI
            Stats(705.7586f, 498.9274f, 53.4522f), // VI+
        )

        val AVERAGE_BEM_STATS = listOf(
            Stats(0f, 0f,0f), // I
            Stats(0f, 0f,0f), // II
            Stats(4657.5f, 3005.5f,1022.5f), // III
            Stats(5171.528f, 3496.5278f,1222.9167f), // IV
            Stats(5664.4f, 4408f,1505.7333f), // V
            Stats(5972.2974f, 5540.5405f,1976.7568f), // VI
            Stats(6314.2856f, 5935.7144f, 2092.8572f), // VI+
        )
    }

    data class Stats(val bp: Float, val hp: Float, val ap: Float)

    suspend fun convertSpeciesEntity(speciesEntity: SpeciesEntity): SpeciesEntity {
        val existingBemVersion = getBEMOfSameSpecies(speciesEntity.spriteDirName)
        existingBemVersion?.let {
            return speciesEntity.copy(phase = existingBemVersion.phase, bp = existingBemVersion.bp, hp = existingBemVersion.hp, ap = existingBemVersion.ap)
        }
        val dimPhase = speciesEntity.phase
        val bemPhase = withContext(Dispatchers.IO) {
            newSpeciesPhase(speciesEntity.cardName, speciesEntity.characterId, dimPhase)
        }
        Log.i(TAG, "Phase ${dimPhase} to $bemPhase")
        var dimBpPhase = dimPhase
        var dimHpPhase = dimPhase
        var dimApPhase = dimPhase
        var bemBpPhase = bemPhase
        var bemHpPhase = bemPhase
        var bemApPhase = bemPhase
        if(bemPhase == dimPhase && dimPhase < AVERAGE_DIM_STATS.size-1) {
            // Lucemon and a few others with abnormally high stats for their phase
            if(speciesEntity.bp > AVERAGE_DIM_STATS[dimPhase+1].bp) {
                Log.i(TAG, "bp stat upgrade")
                dimBpPhase = dimPhase + 1
                bemBpPhase = dimPhase + 1
            }
            if(speciesEntity.hp > AVERAGE_DIM_STATS[dimPhase+1].hp) {
                Log.i(TAG, "hp stat upgrade")
                dimHpPhase = dimPhase + 1
                bemHpPhase = dimPhase + 1
            }
            if(speciesEntity.ap > AVERAGE_DIM_STATS[dimPhase+1].ap) {
                Log.i(TAG, "ap stat upgrade")
                dimApPhase = dimPhase + 1
                bemApPhase = dimPhase + 1
            }
        }
        val bp = convertStat(speciesEntity.bp, dimBpPhase, bemBpPhase, Stats::bp)
        val ap = convertStat(speciesEntity.ap, dimApPhase, bemApPhase, Stats::ap)
        val hp = convertStat(speciesEntity.hp, dimHpPhase, bemHpPhase, Stats::hp)
        Log.i(TAG, "Card ${speciesEntity.cardName} slot: ${speciesEntity.characterId} converted to new stats: {phase: $bemPhase, bp: $bp, hp: $hp, ap: $ap}")
        return speciesEntity.copy(bp = bp, hp = hp, ap = ap, phase = bemPhase)
    }

    private suspend fun getBEMOfSameSpecies(spriteDir: String): SpeciesEntity? {
        val speciesEntities =  withContext(Dispatchers.IO) {
            statConversionDao.getSameSpeciesFromBEM(spriteDir)
        }
        if(speciesEntities.isEmpty()) {
            return null
        }
        return speciesEntities[0]
    }

    private fun convertStat(stat: Int, dimPhase: Int, bemPhase: Int, statSelector: (Stats)->Float): Int {
        val dimMin = statSelector.invoke(MIN_DIM_STATS[dimPhase])
        val dimMax = statSelector.invoke(MAX_DIM_STATS[dimPhase])
        val dimRange = dimMax - dimMin
        if(dimRange == 0f) {
            return statSelector(AVERAGE_BEM_STATS[bemPhase]).toInt()
        }
        val percent = (stat - dimMin)/dimRange
        val bemStddev = statSelector.invoke(STDDEV_BEM_STATS[bemPhase])
        val bemRange = bemStddev * 2
        val bemAverage = statSelector.invoke(AVERAGE_BEM_STATS[bemPhase])
        if(bemRange == 0f) {
            return bemAverage.toInt()
        }
        return ((percent * bemRange) + (bemAverage - bemStddev)).toInt()
    }

    private suspend fun newSpeciesPhase(cardName: String, slotId: Int, currentPhase: Int): Int {
        // Detect Omegamon, Omegamon Zwart, Ragna Lordmon, and Susanoomon
        return withContext(Dispatchers.IO) {
            val maxJogressFromPhase =
                statConversionDao.getFusionToCharacterPhase(cardName, slotId) ?: (currentPhase - 1)
            Log.i(TAG, "Max Jogress From Phase: $maxJogressFromPhase")
            val maxDirectTransformationFromPhase =
                statConversionDao.getTransformationsToCharacterPhase(cardName, slotId)
                    ?: (currentPhase - 1)
            Log.i(TAG, "Max Transform From Phase: $maxDirectTransformationFromPhase")
            val maxTransformFrom = max(maxJogressFromPhase, maxDirectTransformationFromPhase)
            val minPhaseOfNext =
                statConversionDao.getNextTransformationPhaseFromCharacter(cardName, slotId)
                    ?: (currentPhase + 1)
            if(minPhaseOfNext <= currentPhase) { // Handle Aderek's custom periodic mode change DIM
                currentPhase
            } else if(maxTransformFrom >= currentPhase) {
                maxTransformFrom + 1
            } else {
                currentPhase
            }
        }
    }
}