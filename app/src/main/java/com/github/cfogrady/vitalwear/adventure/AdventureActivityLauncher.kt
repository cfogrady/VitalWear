package com.github.cfogrady.vitalwear.adventure

import android.content.Context
import android.content.Intent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.battle.data.BattleResult
import com.github.cfogrady.vitalwear.common.util.ActivityHelper

class AdventureActivityLauncher(
    val launchMenu: () -> Unit,
    val launchBattle: ((Intent) -> Unit) -> Unit) {

    companion object {
        fun buildFromContextAndActivityHelper(context: Context, activityHelper: ActivityHelper): AdventureActivityLauncher {
            val adventureBattle = activityHelper.getActivityLauncherWithResultHandling(
                BattleActivity::class.java) {
                val ordinal = it.data?.extras?.getInt(BattleActivity.RESULT) ?: BattleResult.RETREAT.ordinal
                val result = BattleResult.values()[ordinal]
                (context as VitalWearApp).adventureService.completeBattle(context, result)
            }
            return AdventureActivityLauncher(
                activityHelper.getActivityLauncher(AdventureMenuActivity::class.java),
                adventureBattle,
            )
        }
    }
}