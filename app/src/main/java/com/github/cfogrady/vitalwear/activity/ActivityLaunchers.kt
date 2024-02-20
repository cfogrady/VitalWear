package com.github.cfogrady.vitalwear.activity

import android.content.Context
import com.github.cfogrady.vitalwear.adventure.AdventureActivityLauncher

class ActivityLaunchers(
    val context: Context,
    val firmwareLoadingLauncher: ()-> Unit,
    val statsMenuLauncher: ()-> Unit,
    val trainingMenuLauncher: ()-> Unit,
    val characterSelectionLauncher: ()-> Unit,
    val battleLauncher: () -> Unit,
    val transformLauncher: () -> Unit,
    val sleepToggle: () -> Unit,
    val debugActivityLauncher: () -> Unit,
    val stopBackgroundTrainingLauncher: () -> Unit,
    val toastLauncher: (String) -> Unit,
    val adventureActivityLauncher: AdventureActivityLauncher,
) {

}