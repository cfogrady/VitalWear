package com.github.cfogrady.vitalwear.training

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.background.BackgroundManager
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.util.flow.transformState
import timber.log.Timber

class TrainingMenuActivity : ComponentActivity() {

    private lateinit var bitmapScaler: BitmapScaler
    private lateinit var firmware: TrainingFirmwareSprites
    private lateinit var backgroundManager: BackgroundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        doPermissionChecks()
        val activityHelper = ActivityHelper(this)
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        firmware = (application as VitalWearApp).firmwareManager.getFirmware().value!!.trainingFirmwareSprites
        backgroundManager = (application as VitalWearApp).backgroundManager
        val vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        val trainingLauncher = activityHelper.getActivityLauncherWithResultHandling(TrainingActivity::class.java) {
            val finishToMenu = it.data?.extras?.getBoolean(TrainingActivity.FINISH_TO_MENU, true)
            if (finishToMenu != true) {
                finish()
            }
        }
        val backgroundOnLoad = backgroundManager.selectedBackground.value!! //do here because value shouldn't be called within composable
        setContent {
            val background by backgroundManager.selectedBackground.transformState(backgroundOnLoad) {
                if(it != null) {
                    emit(it)
                }
            }.collectAsStateWithLifecycle()
            vitalBoxFactory.VitalBox {
                bitmapScaler.ScaledBitmap(bitmap = background, contentDescription = "Background", alignment = Alignment.BottomCenter)
                val pagerState = rememberPagerState(pageCount = {4})
                VerticalPager(state = pagerState) { page ->
                    when (page) {
                        0 -> {
                            val trainingType = TrainingType.SQUAT
                            MenuItem(trainingIcon = firmware.squatIcon, trainingText = firmware.squatText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                        1 -> {
                            val trainingType = TrainingType.CRUNCH
                            MenuItem(trainingIcon = firmware.crunchIcon, trainingText = firmware.crunchText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                        2 -> {
                            val trainingType = TrainingType.PUNCH
                            MenuItem(trainingIcon = firmware.punchIcon, trainingText = firmware.punchText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                        3 -> {
                            val trainingType = TrainingType.DASH
                            MenuItem(trainingIcon = firmware.dashIcon, trainingText = firmware.dashText, timeInSeconds = trainingType.durationSeconds) {
                                trainingLauncher.invoke { trainingIntent ->
                                    trainingIntent.putExtra(TrainingActivity.TRAINING_TYPE, trainingType)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun MenuItem(trainingIcon: Bitmap, trainingText: Bitmap, timeInSeconds: Int, onClick: () -> Unit) {
        Column(modifier = Modifier.fillMaxSize().clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            bitmapScaler.ScaledBitmap(bitmap = trainingIcon, contentDescription = "Training Icon", modifier = Modifier.padding(5.dp))
            bitmapScaler.ScaledBitmap(bitmap = trainingText, contentDescription = "Training Text")
            Text(text = "$timeInSeconds", fontWeight = FontWeight.Bold, fontSize = 4.em, color = Color.Yellow, modifier = Modifier.padding(top = 10.dp))
            Text(text = "sec", fontSize = 3.em, color = Color.Yellow)
        }
    }

    private fun doPermissionChecks() {
        val missingPermissions = getMissingPermissionsNeeded()
        val missingBackgroundPermissions = getMissingBackgroundPermissions()
        val backgroundPermissionRequestLauncher = buildBackgroundPermissionRequestLauncher(missingBackgroundPermissions)
        if(missingPermissions.isNotEmpty()) {
            buildPermissionRequestLauncher { requestedPermissions->
                val deniedPermissions = mutableListOf<String>()
                for(requestedPermission in requestedPermissions) {
                    if(!requestedPermission.value) {
                        deniedPermissions.add(requestedPermission.key)
                    }
                }
                if(deniedPermissions.isNotEmpty()) {
                    Toast.makeText(this, "Permission Required For Training", Toast.LENGTH_SHORT).show()
                    Timber.i("Permissions not provided for: $deniedPermissions")
                    finish()
                } else if(missingBackgroundPermissions.isNotEmpty()) {
                    backgroundPermissionRequestLauncher.launch(missingBackgroundPermissions.toTypedArray())
                }
            }.launch(missingPermissions.toTypedArray())
        } else if(missingBackgroundPermissions.isNotEmpty()) {
            backgroundPermissionRequestLauncher.launch(missingBackgroundPermissions.toTypedArray())
        }
    }

    private fun buildBackgroundPermissionRequestLauncher(missingBackgroundPermissions: List<String>): ActivityResultLauncher<Array<String>> {
        return buildPermissionRequestLauncher { requestedPermissions->
            val deniedPermissions = mutableListOf<String>()
            for(requestedPermission in requestedPermissions) {
                if(!requestedPermission.value) {
                    deniedPermissions.add(requestedPermission.key)
                }
            }
            if(deniedPermissions.isNotEmpty()) {
                Toast.makeText(this, "Permission Required For Training", Toast.LENGTH_SHORT).show()
                Timber.i("Permissions not provided for: $deniedPermissions")
                finish()
            }
        }
    }

    private fun getMissingPermissionsNeeded(): List<String> {
        val missingPermissions = mutableListOf<String>()
        if(Build.VERSION.SDK_INT > 28) {
            if (!hasPermission(Manifest.permission.ACTIVITY_RECOGNITION)) {
                missingPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }
        if (!hasPermission(Manifest.permission.BODY_SENSORS)) {
            missingPermissions.add(Manifest.permission.BODY_SENSORS)
        }
        if (!hasPermission(Manifest.permission.WAKE_LOCK)) {
            missingPermissions.add(Manifest.permission.WAKE_LOCK)
        }
        if (!hasPermission(Manifest.permission.FOREGROUND_SERVICE)) {
            missingPermissions.add(Manifest.permission.FOREGROUND_SERVICE)
        }

        return missingPermissions
    }

    // backgound permissions must be requested after regular permissions of the same name
    // or else the permission requester won't launch... for some reason. So do a separate
    // check for background permissions.
    private fun getMissingBackgroundPermissions(): List<String> {
        val missingPermissions = mutableListOf<String>()
        if(Build.VERSION.SDK_INT >= 33) {
            if (!hasPermission(Manifest.permission.BODY_SENSORS_BACKGROUND)) {
                missingPermissions.add(Manifest.permission.BODY_SENSORS_BACKGROUND)
            }
        }
        if(Build.VERSION.SDK_INT >= 34) {
            if (!hasPermission(Manifest.permission.FOREGROUND_SERVICE_HEALTH)) {
                missingPermissions.add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
            }
        }
        return missingPermissions
    }

    private fun hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun buildPermissionRequestLauncher(resultBehavior: (Map<String, Boolean>)->Unit): ActivityResultLauncher<Array<String>> {
        val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
        val launcher = registerForActivityResult(multiplePermissionsContract, resultBehavior)
        return launcher
    }
}