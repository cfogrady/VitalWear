package com.github.cfogrady.vitalwear.vitals

import android.util.Log
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.character.data.Mood
import com.github.cfogrady.vitalwear.complications.ComplicationRefreshService
import com.github.cfogrady.vitalwear.steps.SensorStepService
import com.github.cfogrady.vitalwear.steps.StepChangeListener
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.LinkedList

class VitalService(private val characterManager: CharacterManager, private val complicationRefreshService: ComplicationRefreshService, private val bootTime: LocalDateTime = LocalDateTime.now()) : StepChangeListener {
    companion object {
        const val STEPS_PER_VITAL = 50
    }

    private var remainingSteps = STEPS_PER_VITAL

    private var debugList = LinkedList<Pair<LocalDateTime, String>>()

    fun debug(): List<Pair<String, String>> {
        val debugInfo = ArrayList<Pair<String, String>>()
        debugInfo.add(Pair("Last Reboot", bootTime.toString()))
        debugInfo.addAll(debugList.map {
            Pair(it.first.toLocalTime().toString(), it.second)
        })
        return debugInfo
    }

    override fun processStepChanges(oldSteps: Int, newSteps: Int) {
        transformStepsIntoVitals(oldSteps, newSteps)
    }

    private fun transformStepsIntoVitals(oldSteps: Int, newSteps: Int) {
        val character = getCharacter()
        if(character != BEMCharacter.DEFAULT_CHARACTER) {
            var stepsAlreadyTransformed = oldSteps
            if(newSteps - oldSteps >= remainingSteps) {
                stepsAlreadyTransformed += remainingSteps
                var newVitals = vitalGainModifier(character, 4)
                newVitals += vitalGainModifier(character, 4 * ((newSteps - stepsAlreadyTransformed)/STEPS_PER_VITAL))
                remainingSteps = (newSteps - stepsAlreadyTransformed) % STEPS_PER_VITAL
                addStepChangeToDebug(oldSteps, newSteps, newVitals)
                addVitals(character, newVitals)
            }
        }
    }

    private fun addStepChangeToDebug(oldSteps: Int, newSteps: Int, newVitals: Int) {
        clearOldDebugEntries()
        debugList.addLast(Pair<LocalDateTime, String>(LocalDateTime.now(), "Add $newVitals for step change from $oldSteps to $newSteps"))
    }

    private fun clearOldDebugEntries() {
        while(debugList.isNotEmpty() && debugList.first().first < LocalDate.now().atStartOfDay()) {
            debugList.removeFirst()
        }
    }

    private fun addVitals(character: BEMCharacter, newVitals: Int) {
        character.addVitals(newVitals)
        complicationRefreshService.refreshVitalsComplication()
    }

    private fun getCharacter() : BEMCharacter {
        return characterManager.getCurrentCharacter()
    }

    private fun vitalGainModifier(character: BEMCharacter, vitals: Int) : Int {
        if(character == BEMCharacter.DEFAULT_CHARACTER) {
            Log.w(SensorStepService.TAG, "Cannot apply vitals gain modifier for null active character.")
            return vitals
        }
        if(character.characterStats.injured) {
            return vitals/2
        }
        return when(character.mood()) {
            Mood.NORMAL -> vitals
            Mood.GOOD -> vitals * 2
            Mood.BAD -> vitals/2
        }
    }



    private val vitalWinTable = arrayOf(
        intArrayOf(200, 300, 600, 1200, 1800, 2400), //phase 3 (index 2)
        intArrayOf(100, 300, 450, 700, 1400, 2100), //phase 4 (index 3)
        intArrayOf(20, 150, 400, 600, 800, 1600), //phase 5 (index 4)
        intArrayOf(20, 20, 400, 500, 750, 900), //phase 6 (index 5)
        intArrayOf(20, 20, 20, 500, 600, 800), //phase 7 (index 6)
        intArrayOf(20, 20, 20, 20, 600, 700), //phase 8 (index 7)
    )

    private val vitalLossTable = arrayOf(
        intArrayOf(-160, -100, -20, -20, -20, -20), //phase 3 (index 2)
        intArrayOf(-300, -240, -150, -20, -20, -20), //phase 4 (index 3)
        intArrayOf(-600, -450, -320, -400, -20, -20), //phase 5 (index 4)
        intArrayOf(-1200, -700, -600, -400, -500, -20), //phase 6 (index 5)
        intArrayOf(-1800, -1400, -800, -750, -480, -600), //phase 7 (index 6)
        intArrayOf(-2400, -2100, -1600, -900, -800, -560), //phase 8 (index 7)
    )

    fun processVitalChangeFromBattle(partnerLevel: Int, opponentLevel: Int, win: Boolean): Int {
        val vitalsChange = if(win) {
            vitalWinTable[partnerLevel-2][opponentLevel-2]
        } else {
            vitalLossTable[partnerLevel-2][opponentLevel-2]
        }
        addBattleChangeToDebug(opponentLevel, win, vitalsChange)
        addVitals(characterManager.getCurrentCharacter(), vitalsChange)
        return vitalsChange
    }

    private fun addBattleChangeToDebug(opponentLevel: Int, win: Boolean, newVitals: Int) {
        clearOldDebugEntries()
        debugList.addLast(Pair<LocalDateTime, String>(LocalDateTime.now(), "Add $newVitals from battle against level ${opponentLevel+1} opponent. Won: $win"))
    }
}