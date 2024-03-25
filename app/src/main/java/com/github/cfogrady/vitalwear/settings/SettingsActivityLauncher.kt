package com.github.cfogrady.vitalwear.settings

import android.content.Intent

class SettingsActivityLauncher(
    // val sendLog: ()->Unit,
    val toast: (String) -> Unit,
    val backgroundSelection: ((Intent) -> Unit) -> Unit
) {}