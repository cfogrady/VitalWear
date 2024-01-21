package com.github.cfogrady.vitalwear.character.transformation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.github.cfogrady.vitalwear.notification.NotificationChannelManager

class TransformationActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val transformationScreenFactory = (application as VitalWearApp).transformationScreenFactory
        val notificationChannelManager = (application as VitalWearApp).notificationChannelManager
        val character = (application as VitalWearApp).characterManager.getCurrentCharacter()!!
        notificationChannelManager.cancelNotification(NotificationChannelManager.TRANSFORMATION_READY_ID)
        setContent {
            KeepScreenOn()
            transformationScreenFactory.RunTransformation(applicationContext, character){
                finish()
            }
        }
    }
}