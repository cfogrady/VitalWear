package com.github.cfogrady.vitalwear.character.data

import android.util.Log
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Duration
import java.time.LocalDateTime

@Entity(tableName = "character")
data class CharacterEntity (
    //TODO: Change to long
    @PrimaryKey(autoGenerate = true)
    var id: Int,
    @ColumnInfo(name = "state") var state: CharacterState,
    @ColumnInfo(name = "card_file") var cardFile: String,
    @ColumnInfo(name = "slot_id") var slotId: Int,
    //franchise
    //bem/dim
    @ColumnInfo(name = "last_update") var lastUpdate: LocalDateTime,
    @ColumnInfo(name = "vitals") var vitals: Int,
    @ColumnInfo(name = "training_time_remaining") var trainingTimeRemainingInSeconds: Long,
    @ColumnInfo(name = "has_transformations") var hasTransformations: Boolean,
    @ColumnInfo(name = "time_until_next_transformation") var timeUntilNextTransformation: Long,
    @ColumnInfo(name = "trained_bp") var trainedBp: Int,
    @ColumnInfo(name = "trained_hp") var trainedHp: Int,
    @ColumnInfo(name = "trained_ap") var trainedAp: Int,
    @ColumnInfo(name = "trained_pp") var trainedPP: Int,
    @ColumnInfo(name = "injured") var injured: Boolean,
    @ColumnInfo(name = "lost_battles_injured") var lostBattlesInjured: Int,
    @ColumnInfo(name = "accumulated_daily_injuries") var accumulatedDailyInjuries: Int,
    @ColumnInfo(name = "total_battles") var totalBattles: Int,
    @ColumnInfo(name = "current_phase_battles") var currentPhaseBattles: Int,
    @ColumnInfo(name = "total_wins") var totalWins: Int,
    @ColumnInfo(name = "current_phase_wins") var currentPhaseWins: Int,
    @ColumnInfo(name = "mood") var mood: Int,
    @ColumnInfo(name = "dead") var dead: Boolean,
) {

    companion object {
        const val TAG = "CharacterEntity"
    }
    fun currentPhaseWinRatio(): Int {
        if(currentPhaseBattles == 0) {
            return 0
        }
        return (100 * currentPhaseWins) / currentPhaseBattles
    }

    @Synchronized
    fun updateTimeStamps(now: LocalDateTime) {
        val deltaTimeInSeconds = Duration.between(lastUpdate, now).seconds
        if(deltaTimeInSeconds <= 0) {
            Log.i(TAG, "Already updated to timestamp. Skipping update")
            return
        }
        trainingTimeRemainingInSeconds -= deltaTimeInSeconds
        if(trainingTimeRemainingInSeconds < 0) {
            trainingTimeRemainingInSeconds = 0
        }
        timeUntilNextTransformation -= deltaTimeInSeconds
        if(timeUntilNextTransformation < 0) {
            timeUntilNextTransformation = 0
        }
        lastUpdate = now
    }
}