package com.github.cfogrady.vitalwear.activity

class ActivityLaunchers(
    val firmwareLoadingLauncher: ()-> Unit,
    val statsMenuLauncher: ()-> Unit,
    val trainingMenuLauncher: ()-> Unit,
    val characterSelectionLauncher: ()-> Unit,
    val battleLauncher: () -> Unit,
    val transformLauncher: () -> Unit,
    val debugActivityLauncher: () -> Unit,
) {

}