package com.github.cfogrady.vitalwear.settings

class SettingsActivityLauncher(
    val launchDebug: ()->Unit,
    val toast: (String) -> Unit,
    val backgroundSelection: () -> Unit
    //val launchHeartRateMeasurement: () -> Unit
) {}