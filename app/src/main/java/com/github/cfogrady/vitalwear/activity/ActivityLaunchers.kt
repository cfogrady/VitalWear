package com.github.cfogrady.vitalwear.activity

import android.content.Context

class ActivityLaunchers(
    val context: Context,
    val firmwareLoadingLauncher: ()-> Unit,
    val statsMenuLauncher: ()-> Unit,
    val trainingMenuLauncher: ()-> Unit,
    val adventureMenuLauncher: () -> Unit,
    val characterSelectionLauncher: ()-> Unit,
    val battleLauncher: () -> Unit,
    val transformLauncher: () -> Unit,
    val debugActivityLauncher: () -> Unit,
    val stopBackgroundTrainingLauncher: () -> Unit,
    val toastLauncher: (String) -> Unit,
) {

}