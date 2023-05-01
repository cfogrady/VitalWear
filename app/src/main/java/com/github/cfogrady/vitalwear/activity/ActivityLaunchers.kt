package com.github.cfogrady.vitalwear.activity

import com.github.cfogrady.vitalwear.training.TrainingMenuActivity

class ActivityLaunchers(
    val statsMenuLauncher: ()-> Unit,
    val trainingMenuLauncher: ()-> Unit,
    val characterSelectionLauncher: ()-> Unit,
    val battleLauncher: () -> Unit,
    val debugActivityLauncher: () -> Unit,
) {

}