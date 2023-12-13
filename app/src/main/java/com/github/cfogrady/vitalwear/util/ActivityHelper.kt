package com.github.cfogrady.vitalwear.util

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity

class ActivityHelper {

    val activity: ComponentActivity

    constructor(activity: ComponentActivity) {
        this.activity = activity
    }

    fun getActivityLauncher(activityClass: Class<*>?, addToIntent: (Intent) -> Unit = {_ -> {}}): () -> Unit {
        val intent = Intent(activity.applicationContext, activityClass)
        addToIntent(intent)
        return {activity.startActivity(intent)}
    }
    fun getActivityLauncherWithResultHandling(activityClass : Class<*>?, addToIntent: (Intent) -> Unit = { _ -> {}}, onResult: (ActivityResult) -> Unit): () -> Unit {
        val intent = Intent(activity.applicationContext, activityClass)
        addToIntent(intent)
        val contract = ActivityResultContracts.StartActivityForResult()
        contract.createIntent(activity.applicationContext, intent)
        val activityLauncher = activity.registerForActivityResult(contract) {result ->
            onResult(result)
        }
        return {
            activityLauncher.launch(intent)
        }
    }
}
