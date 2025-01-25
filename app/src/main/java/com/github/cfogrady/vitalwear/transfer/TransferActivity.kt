package com.github.cfogrady.vitalwear.transfer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.wear.compose.material.CompactButton
import androidx.wear.compose.material.Text
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.nearby.connections.p2p.wear.ui.DisplayMatchingDevices
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.protos.Character
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransferActivity: ComponentActivity() {


    enum class TransferState {
        ENTRY,
        FIND_DEVICES,
        CONNECTED,
        TRANSFERRED,
    }

    enum class SendOrReceive {
        SEND,
        RECEIVE
    }

    lateinit var vitalBoxFactory: VitalBoxFactory
    lateinit var transferActivityController: TransferActivityController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        transferActivityController = (application as VitalWearApp).transferActivityController
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
            TransferScreen()
        }
    }

    private fun buildPermissionRequestLauncher(resultBehavior: (Map<String, Boolean>)->Unit): ActivityResultLauncher<Array<String>> {
        val multiplePermissionsContract = ActivityResultContracts.RequestMultiplePermissions()
        val launcher = registerForActivityResult(multiplePermissionsContract, resultBehavior)
        return launcher
    }

    @Composable
    fun TransferScreen() {
        val characterTransfer = remember { CharacterTransfer(this) }
        var state by remember { mutableStateOf(TransferState.ENTRY) }
        var sendOrReceive by remember { mutableStateOf(SendOrReceive.SEND) }
        var result: StateFlow<CharacterTransfer.Result> = remember { MutableStateFlow(CharacterTransfer.Result.TRANSFERRING) }
        when(state) {
            TransferState.ENTRY -> SelectSendOrReceive(onSelect = {
                sendOrReceive = it
                state = TransferState.FIND_DEVICES
            })
            TransferState.FIND_DEVICES -> FindDevices(characterTransfer) {
                if(sendOrReceive == SendOrReceive.SEND) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val character = transferActivityController.getActiveCharacterProto()
                        if(character == null) {
                            runOnUiThread {
                                Toast.makeText(this@TransferActivity, "No active character!", Toast.LENGTH_SHORT).show()
                                characterTransfer.close()
                                finish()
                            }
                        } else {
                            result = characterTransfer.sendCharacterToDevice(it, character)
                        }
                    }
                } else {
                    result = characterTransfer.receiveCharacterFrom(it, this::receiveCharacter)
                }
                state = TransferState.CONNECTED
            }
            TransferState.CONNECTED -> {
                val connectionStatus by result.collectAsState()
                if(connectionStatus != CharacterTransfer.Result.TRANSFERRING) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Transferring...")
                    }
                } else {
                    state = TransferState.TRANSFERRED
                }
            }
            TransferState.TRANSFERRED -> {
                TransferResult(sendOrReceive, result)
            }
        }
    }

    fun receiveCharacter(character: Character): Boolean {
        return true
    }

    @Composable
    fun SelectSendOrReceive(onSelect: (SendOrReceive) -> Unit) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            CompactButton(onClick = {onSelect.invoke(SendOrReceive.SEND)}) {
                Text("Send Character")
            }
            CompactButton(onClick = {onSelect.invoke(SendOrReceive.RECEIVE)}) {
                Text("Receive Character")
            }
        }
    }

    @Composable
    fun FindDevices(characterTransfer: CharacterTransfer, onDeviceFound: (String)->Unit) {
        var discoveredDevices by remember { mutableStateOf(flow<String> {}) }
        var connected by remember { mutableStateOf(false) }
        DisposableEffect(true) {
            discoveredDevices = characterTransfer.searchForOtherTransferDevices()

            onDispose {
                if(!connected) {
                    characterTransfer.close()
                }
            }
        }
        DisplayMatchingDevices(characterTransfer.deviceName, discoveredDevices, rescan = {
            connected = false
            characterTransfer.searchForOtherTransferDevices()
        }, selectDevice = {
            connected = true
            onDeviceFound.invoke(it)
        })
    }

    @Composable
    fun TransferResult(sendOrReceive: SendOrReceive, resultStatusFlow: StateFlow<CharacterTransfer.Result>) {
        val resultStatus = remember { resultStatusFlow.value }
        when(resultStatus) {
            CharacterTransfer.Result.TRANSFERRING -> {
                throw IllegalStateException("Shouldn't be looking at result is the status is still Trasnferring")
            }
            CharacterTransfer.Result.SUCCESS -> {
                if(sendOrReceive == SendOrReceive.SEND) {
                    // animation + remove from device
                } else {
                    // animation
                }
            }
            CharacterTransfer.Result.REJECTED -> {
                Toast.makeText(this, "Transfer Rejected!", Toast.LENGTH_SHORT).show()
                finish()
            }
            CharacterTransfer.Result.FAILURE -> {
                Toast.makeText(this, "Transfer Failed!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}