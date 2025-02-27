package com.github.cfogrady.vitalwear.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vitalwear.SaveData
import com.github.cfogrady.vitalwear.SaveDataRepository
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.adventure.AdventureActivityLauncher
import com.github.cfogrady.vitalwear.battle.BattleActivity
import com.github.cfogrady.vitalwear.character.activity.CharacterSelectActivity
import com.github.cfogrady.vitalwear.character.transformation.TransformationActivity
import com.github.cfogrady.vitalwear.firmware.LoadFirmwareActivity
import com.github.cfogrady.vitalwear.stats.StatsMenuActivity
import com.github.cfogrady.vitalwear.training.TrainingMenuActivity
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.settings.SettingsActivity
import com.github.cfogrady.vitalwear.training.StopBackgroundTrainingActivity
import com.github.cfogrady.vitalwear.transfer.TransferActivity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    companion object {
        val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val saveDataRepository = (application as VitalWearApp).saveDataRepository
        val permissionCheckLauncher = getPermissionCheckLauncher(saveDataRepository)
        lifecycleScope.launch {
            if(!saveDataRepository.saveDataFlow.first().permissionsAlreadyRequested) {
                permissionCheckLauncher()
            }
        }
        val activityLaunchers = buildActivityLaunchers()
        setContent {
            val saveData by saveDataRepository.saveDataFlow.collectAsStateWithLifecycle(SaveData.getDefaultInstance())
            InitialScreen(InitialScreenController.buildInitialScreenController(this, application as VitalWearApp, activityLaunchers))
        }
    }

    override fun onStart() {
        super.onStart()
        val characterManager = (application as VitalWearApp).characterManager
        characterManager.getCurrentCharacter()?.characterStats?.updateTimeStamps(LocalDateTime.now())
    }

    override fun onStop() {
        super.onStop()
    }

    private fun buildActivityLaunchers(): ActivityLaunchers {
        val activityHelper = ActivityHelper(this)
        return ActivityLaunchers(
            activityHelper.getActivityLauncher(LoadFirmwareActivity::class.java),
            activityHelper.getActivityLauncher(StatsMenuActivity::class.java),
            activityHelper.getActivityLauncher(TrainingMenuActivity::class.java),
            activityHelper.getActivityLauncher(CharacterSelectActivity::class.java),
            activityHelper.getActivityLauncher(BattleActivity::class.java),
            activityHelper.getActivityLauncher(TransferActivity::class.java),
            activityHelper.getActivityLauncher(TransformationActivity::class.java),
            activityHelper.getActivityLauncher(SettingsActivity::class.java),
            activityHelper.getActivityLauncher(StopBackgroundTrainingActivity::class.java),
            {text -> Toast.makeText(this, text, Toast.LENGTH_SHORT).show() },
            AdventureActivityLauncher.buildFromContextAndActivityHelper(application, activityHelper),
        )
    }

    private fun getPermissionCheckLauncher(saveDataRepository: SaveDataRepository): ()->Unit {
        val missingPermissions = getMissingPermissionsNeeded()
        val missingBackgroundPermissions = getMissingBackgroundPermissions()
        val backgroundPermissionRequestLauncher = buildBackgroundPermissionRequestLauncher(saveDataRepository)
        if(missingPermissions.isNotEmpty()) {
            val permissionRequestLauncher = buildPermissionRequestLauncher { requestedPermissions->
                val deniedPermissions = mutableListOf<String>()
                for(requestedPermission in requestedPermissions) {
                    if(!requestedPermission.value) {
                        deniedPermissions.add(requestedPermission.key)
                    }
                }
                if(deniedPermissions.isNotEmpty()) {
                    Toast.makeText(this, "Denied permissions may cause bugs", Toast.LENGTH_SHORT).show()
                    Timber.i("Permissions not provided for: $deniedPermissions")
                    lifecycleScope.launch {
                        saveDataRepository.togglePermissionsAlreadyRequested()
                    }
                } else if(missingBackgroundPermissions.isNotEmpty()) {
                    backgroundPermissionRequestLauncher.launch(missingBackgroundPermissions.toTypedArray())
                }
            }
            return {permissionRequestLauncher.launch(missingPermissions.toTypedArray())}
        } else if(missingBackgroundPermissions.isNotEmpty()) {
            return {backgroundPermissionRequestLauncher.launch(missingBackgroundPermissions.toTypedArray())}
        }
        return {}
    }

    private fun buildBackgroundPermissionRequestLauncher(saveDataRepository: SaveDataRepository): ActivityResultLauncher<Array<String>> {
        return buildPermissionRequestLauncher { requestedPermissions->
            val deniedPermissions = mutableListOf<String>()
            for(requestedPermission in requestedPermissions) {
                if(!requestedPermission.value) {
                    deniedPermissions.add(requestedPermission.key)
                }
            }
            if(deniedPermissions.isNotEmpty()) {
                Toast.makeText(this, "Denied background permissions may cause bugs", Toast.LENGTH_SHORT).show()
                Timber.i("Permissions not provided for: $deniedPermissions")
                finish()
            }
            lifecycleScope.launch {
                saveDataRepository.togglePermissionsAlreadyRequested()
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
        if(Build.VERSION.SDK_INT >= 33) {
            if (!hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
                missingPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if(Build.VERSION.SDK_INT >= 34) {
            if (!hasPermission(Manifest.permission.FOREGROUND_SERVICE_HEALTH)) {
                missingPermissions.add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
            }
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