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

        val MIN_BEM_STATS = listOf(
            Stats(0f, 0f,0f), // I
            Stats(0f, 0f,0f), // II
            Stats(3900f, 2600f,900f), // III
            Stats(3500f, 3000f,1100f), // IV
            Stats(3800f, 3100f,1250f), // V
            Stats(3200f, 4150f,1800f), // VI
            Stats(5000f, 4900f, 2000f), // VI+
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

        val MAX_BEM_STATS = listOf(
            Stats(0f, 0f,0f), // I
            Stats(0f, 0f,0f), // II
            Stats(5300f, 3800f,1200f), // III
            Stats(6500f, 4000f,1350f), // IV
            Stats(8000f, 5100f,1700f), // V
            Stats(6900f, 6400f,2200f), // VI
            Stats(7000f, 6500f, 2150f), // VI+
        )
    }

    data class Stats(val bp: Float, val hp: Float, val ap: Float)

    suspend fun convertSpeciesEntity(speciesEntity: SpeciesEntity): SpeciesEntity {
        val newPhase = withContext(Dispatchers.IO) {
            newSpeciesPhase(speciesEntity.cardName, speciesEntity.characterId, speciesEntity.phase)
        }
        Log.i(TAG, "Phase ${speciesEntity.phase} to $newPhase")
        var bpPhase = newPhase
        var hpPhase = newPhase
        var apPhase = newPhase
        if(newPhase == AVERAGE_DIM_STATS.size - 1) {
            // Lucemon and a few others with abnormally high stats for their phase
            bpPhase = if(speciesEntity.bp > AVERAGE_DIM_STATS[newPhase+1].bp) newPhase + 1 else newPhase
            hpPhase = if(speciesEntity.hp > AVERAGE_DIM_STATS[newPhase+1].hp) newPhase + 1 else newPhase
            apPhase = if(speciesEntity.ap > AVERAGE_DIM_STATS[newPhase+1].ap) newPhase + 1 else newPhase
        }
        val bp = convertStat(speciesEntity.bp, bpPhase, Stats::bp)
        val hp = convertStat(speciesEntity.hp, hpPhase, Stats::hp)
        val ap = convertStat(speciesEntity.ap, apPhase, Stats::ap)
        Log.i(TAG, "Card ${speciesEntity.cardName} slot: ${speciesEntity.characterId} converted to new stats: {phase: $newPhase, bp: $bp, hp: $hp, ap: $ap}")
        return speciesEntity.copy(bp = bp, hp = hp, ap = ap, phase = newPhase)
    }

    private fun convertStat(stat: Int, phase: Int, statSelector: (Stats)->Float): Int {
        val dimMin = statSelector.invoke(MIN_DIM_STATS[phase])
        val dimMax = statSelector.invoke(MAX_DIM_STATS[phase])
        val dimRange = dimMax - dimMin
        if(dimRange == 0f) {
            return statSelector(AVERAGE_BEM_STATS[phase]).toInt()
        }
        val percent = (stat - dimMin)/dimRange
        val bemMin = statSelector.invoke(MIN_BEM_STATS[phase])
        val bemMax = statSelector.invoke(MAX_BEM_STATS[phase])
        val bemRange = bemMax - bemMin
        if(bemRange == 0f) {
            return statSelector(AVERAGE_BEM_STATS[phase]).toInt()
        }
        return ((percent * bemRange) + bemMin).toInt()
    }

    private suspend fun newSpeciesPhase(cardName: String, slotId: Int, currentPhase: Int): Int {
        // Detect Omegamon, Omegamon Zwart, Ragna Lordmon, and Susanoomon
        return withContext(Dispatchers.IO) {
            val maxJogressFromPhase =
                statConversionDao.getFusionPhasesToCharacter(cardName, slotId) ?: (currentPhase - 1)
            Log.i(TAG, "Max Jogress From Phase: $maxJogressFromPhase")
            val maxDirectTransformationFromPhase =
                statConversionDao.getPhaseTransformationsToCharacter(cardName, slotId)
                    ?: (currentPhase - 1)
            Log.i(TAG, "Max Transform From Phase: $maxDirectTransformationFromPhase")
            val maxTransformFrom = max(maxJogressFromPhase, maxDirectTransformationFromPhase)
            if(maxTransformFrom >= currentPhase) {
                maxTransformFrom + 1
            } else {
                currentPhase
            }
        }
    }
}