package com.github.cfogrady.vitalwear.menu

import android.content.Context
import com.github.cfogrady.vitalwear.adventure.AdventureActivityLauncher

class ActivityLaunchers(
    val firmwareLoadingLauncher: ()-> Unit = {},
    val statsMenuLauncher: ()-> Unit = {},
    val trainingMenuLauncher: ()-> Unit = {},
    val characterSelectionLauncher: ()-> Unit = {},
    val battleLauncher: () -> Unit = {},
    val transferLauncher: () -> Unit = {},
    val transformLauncher: () -> Unit = {},
    val settingsActivityLauncher: () -> Unit = {},
    val stopBackgroundTrainingLauncher: () -> Unit = {},
    val toastLauncher: (String) -> Unit = {},
    val adventureActivityLauncher: AdventureActivityLauncher = AdventureActivityLauncher(),
    val context: Context
) {

}