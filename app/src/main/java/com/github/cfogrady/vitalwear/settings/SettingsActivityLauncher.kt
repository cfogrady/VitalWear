package com.github.cfogrady.vitalwear.settings

import android.content.Intent

class SettingsActivityLauncher(
    val launchDebug: ()->Unit,
    val sendLog: ()->Unit,
    val toast: (String) -> Unit,
    val backgroundSelection: ((Intent) -> Unit) -> Unit
    //val launchHeartRateMeasurement: () -> Unit
) {}