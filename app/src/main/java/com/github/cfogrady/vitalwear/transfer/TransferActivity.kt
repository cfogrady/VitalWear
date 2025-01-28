package com.github.cfogrady.vitalwear.transfer

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.nearby.connections.p2p.wear.ui.DisplayMatchingDevices
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.protos.Character
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    lateinit var transferBackground: Bitmap
    lateinit var bitmapScaler: BitmapScaler
    var backgroundHeight: Dp = 0.dp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vitalBoxFactory = (application as VitalWearApp).vitalBoxFactory
        transferActivityController = (application as VitalWearApp).transferActivityController
        transferBackground = (application as VitalWearApp).firmwareManager.getFirmware().value!!.transformationFirmwareSprites.rayOfLightBackground
        bitmapScaler = (application as VitalWearApp).bitmapScaler
        backgroundHeight = (application as VitalWearApp).backgroundHeight
        val missingPermissions = NearbyP2PConnection.getMissingPermissions(this)
        if(missingPermissions.isNotEmpty()) {
            buildPermissionRequestLauncher { requestedPermissions->
                val deniedPermissions = mutableListOf<String>()
                for(requestedPermission in requestedPermissions) {
                    if(!requestedPermission.value) {
                        deniedPermissions.add(requestedPermission.key)
                    }
                }
                if(deniedPermissions.isNotEmpty()) {
                    Toast.makeText(this, "Permission Required For Transfers", Toast.LENGTH_SHORT).show()
                    finish()
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
        val coroutineScope = rememberCoroutineScope()
        val characterTransfer = remember {
            CharacterTransfer.getInstance(this)
        }
        var state by remember { mutableStateOf(TransferState.ENTRY) }
        var sendOrReceive by remember { mutableStateOf(SendOrReceive.SEND) }
        var result = remember { MutableStateFlow(CharacterTransfer.Result.TRANSFERRING) }
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
                            val transferResult = characterTransfer.sendCharacterToDevice(it, character)
                            coroutineScope.launch {
                                transferResult.collect{ transferResultValue ->
                                    result.update { transferResultValue }
                                }
                            }
                        }
                    }
                } else {
                    val transferResult = characterTransfer.receiveCharacterFrom(it, this::receiveCharacter)
                    coroutineScope.launch {
                        transferResult.collect{ transferResultValue ->
                            result.update { transferResultValue }
                        }
                    }
                }
                state = TransferState.CONNECTED
            }
            TransferState.CONNECTED -> {
                val connectionStatus by result.collectAsState()
                if(connectionStatus == CharacterTransfer.Result.TRANSFERRING) {
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

    var receiveCharacterSprites: TransferActivityController.ReceiveCharacterSprites? = null

    suspend fun receiveCharacter(character: Character): Boolean {
        val receivedCharacterSprites = transferActivityController.receiveCharacter(this, character)
        if(receivedCharacterSprites == null) {
            return false
        }
        receiveCharacterSprites = receivedCharacterSprites
        return true
    }

    @Preview(
        device = WearDevices.LARGE_ROUND,
        showSystemUi = true,
        backgroundColor = 0xff000000,
        showBackground = true
    )
    @Composable
    fun SelectSendOrReceive(onSelect: (SendOrReceive) -> Unit = {}) {
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
        val discoveredDevices = remember { MutableSharedFlow<String>() }
        val coroutineScope = rememberCoroutineScope()
        var connected by remember { mutableStateOf(false) }
        DisposableEffect(true) {
            coroutineScope.launch {
                characterTransfer.searchForOtherTransferDevices().collect {
                    discoveredDevices.emit(it)
                } 
            }

            onDispose {
                if(!connected) {
                    characterTransfer.close()
                }
            }
        }
        DisplayMatchingDevices(characterTransfer.deviceName, discoveredDevices, rescan = {
            connected = false
            characterTransfer.close()
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
                    val activeCharacter = transferActivityController.getActiveCharacter()!!
                    LaunchedEffect(true) {
                        transferActivityController.deleteActiveCharacter()
                    }
                    val idle = activeCharacter.characterSprites.sprites[CharacterSprites.IDLE_1]
                    val walk = activeCharacter.characterSprites.sprites[CharacterSprites.WALK_1]
                    SendAnimation(idleBitmap = idle, walkBitmap = walk) { finish() }
                } else {
                    ReceiveAnimation(receiveCharacterSprites!!.idle, receiveCharacterSprites!!.happy) { finish() }
                    Toast.makeText(this, "Transfer Recevied!", Toast.LENGTH_SHORT).show()
                    finish()
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

    @Composable
    fun SendAnimation(idleBitmap: Bitmap, walkBitmap: Bitmap, onComplete: ()->Unit) {
        var targetAnimation by remember { mutableStateOf(0) }
        var idle by remember { mutableStateOf(true) }
        val flicker by animateIntAsState(targetAnimation, tween(
            durationMillis = 3000,
            easing = FastOutLinearInEasing
        )) {
            if(it == 11) {
                onComplete.invoke()
            }
        }
        LaunchedEffect(true) {
            delay(500)
            idle = false
            delay(500)
            targetAnimation = 11
        }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(transferBackground, "Background", alignment = Alignment.BottomCenter)

            if(flicker % 2 == 0) {
                bitmapScaler.ScaledBitmap(if(idle) idleBitmap else walkBitmap, "Character", alignment = Alignment.BottomCenter,
                    modifier = Modifier.offset(y = backgroundHeight.times(-0.05f)))
            }
        }
    }

    @Composable
    fun ReceiveAnimation(idleBitmap: Bitmap, happyBitmap: Bitmap, onComplete: () -> Unit) {
        var targetAnimation by remember { mutableStateOf(0) }
        var idle by remember { mutableStateOf(false) }
        var startIdleFlip by remember { mutableStateOf(false) }
        val flicker by animateIntAsState(targetAnimation, tween(
            durationMillis = 3000,
            easing = LinearOutSlowInEasing
        )) {
            if(it == 11) {
                startIdleFlip = true
            }
        }
        LaunchedEffect(true) {
            targetAnimation = 11
        }
        LaunchedEffect(startIdleFlip) {
            if(startIdleFlip) {
                idle = true
                delay(500)
                idle = false
                delay(500)
                idle = true
                delay(500)
                idle = false
                onComplete.invoke()

            }
        }
        vitalBoxFactory.VitalBox {
            bitmapScaler.ScaledBitmap(transferBackground, "Background", alignment = Alignment.BottomCenter)

            if(flicker % 2 == 1) {
                bitmapScaler.ScaledBitmap(if(idle) idleBitmap else happyBitmap, "Character", alignment = Alignment.BottomCenter,
                    modifier = Modifier.offset(y = backgroundHeight.times(-0.05f)))
            }
        }
    }
}