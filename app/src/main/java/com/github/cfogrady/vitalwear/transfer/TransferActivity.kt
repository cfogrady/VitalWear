package com.github.cfogrady.vitalwear.transfer

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Text
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.nearby.connections.p2p.wear.ui.DisplayMatchingDevices
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import kotlinx.coroutines.flow.MutableStateFlow

class TransferActivity: ComponentActivity() {


    enum class TransferState {
        ENTRY,
        SEND,
        RECEIVE,
    }

    val transferState = MutableStateFlow(TransferState.ENTRY)

    lateinit var vitalBoxFactory: VitalBoxFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        val missingPermissions = NearbyP2PConnection.getMissingPermissions(this)
        if(missingPermissions.isNotEmpty()) {
            buildPermissionRequestLauncher { grantedPermissions->
                for(grantedPermission in grantedPermissions) {
                    if(!grantedPermission.value) {
                        Toast.makeText(this, "Transfers requires all requested permissions to be enabled to run", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
            }.launch(missingPermissions.toTypedArray())
        }
        setContent {
            val state by transferState.collectAsState()
            when(state) {
                TransferState.ENTRY -> SelectSendOrReceive()
                TransferState.SEND -> Send()
                TransferState.RECEIVE -> TODO()
            }
        }
    }

    private fun buildPermissionRequestLauncher(resultBehavior: (Map<String, Boolean>)->Unit): ActivityResultLauncher<Array<String>> {
        val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
        val launcher = registerForActivityResult(multiplePermissionsContract, resultBehavior)
        return launcher
    }

    @Composable
    fun SelectSendOrReceive() {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CompactButton(onClick = {transferState.value = TransferState.SEND}) {
                Text("Send Character")
            }
            CompactButton(onClick = {transferState.value = TransferState.RECEIVE}) {
                Text("Receive Character")
            }
        }
    }

    @Composable
    fun Send() {
        val nearbyP2PConnection = remember {
            NearbyP2PConnection(this, "")
        }
        DisplayMatchingDevices("", nearbyP2PConnection.discoveredDevices, rescan = {}, selectDevice = {
            
        })
    }
}