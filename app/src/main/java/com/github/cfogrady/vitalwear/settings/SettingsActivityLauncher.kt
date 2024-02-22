package com.github.cfogrady.vitalwear.settings

import android.content.Intent

class SettingsActivityLauncher(
    val launchDebug: ((Intent?) -> Unit)->Unit,
    val toast: (String) -> Unit,
    //val launchHeartRateMeasurement: () -> Unit
) {}