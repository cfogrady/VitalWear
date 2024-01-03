package com.github.cfogrady.vitalwear.common.util

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class ActivityHelper(val activity: ComponentActivity) {

    fun getActivityLauncher(activityClass: Class<*>?, addToIntent: (Intent) -> Unit = {_ -> {}}): () -> Unit {
        val intent = Intent(activity.applicationContext, activityClass)
        addToIntent(intent)
        return {activity.startActivity(intent)}
    }

    fun getActivityLauncherWithResultHandling(activityClass : Class<*>?, onResult: (ActivityResult) -> Unit): ((Intent) -> Unit) -> Unit {
        val intent = Intent(activity.applicationContext, activityClass)
        val contract = ActivityResultContracts.StartActivityForResult()
        contract.createIntent(activity.applicationContext, intent)
        val activityLauncher = activity.registerForActivityResult(contract) {result ->
            onResult(result)
        }
        return {
            it.invoke(intent)
            activityLauncher.launch(intent)
        }
    }
}
