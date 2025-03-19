package com.github.cfogrady.vitalwear.transfer

import android.graphics.Bitmap
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.wear.compose.material3.CompactButton
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.nearby.connections.p2p.wear.ui.DisplayMatchingDevices
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import com.github.cfogrady.vitalwear.protos.Character
import com.github.cfogrady.vitalwear.transfer.TransferScreenController.ReceiveCharacterSprites
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TransferState {
    ENTRY,
    FIND_DEVICES,
    UNKNOWN_CARD,
    CONNECTED,
    TRANSFERRED,
}

enum class SendOrReceive {
    SEND,
    RECEIVE
}

interface TransferScreenController: SendAnimationController, ReceiveAnimationController {
    fun endActivityWithToast(msg: String)

    suspend fun getActiveCharacterProto(): Character?

    fun getCharacterTransfer(): CharacterTransfer

    fun getActiveCharacter(): VBCharacter?

    fun deleteActiveCharacter()

    fun finish()

    suspend fun receiveCharacter(character: Character): Boolean

    data class ReceiveCharacterSprites(val idle: Bitmap, val happy: Bitmap)
}


@Composable
fun TransferScreen(controller: TransferScreenController) {
    val coroutineScope = rememberCoroutineScope()
    val characterTransfer = remember {
        controller.getCharacterTransfer()
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
                    val character = controller.getActiveCharacterProto()
                    if(character == null) {
                        characterTransfer.close()
                        controller.endActivityWithToast("No active character!")
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
                val transferResult = characterTransfer.receiveCharacterFrom(it, controller::receiveCharacter)
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
            TransferResult(controller, sendOrReceive, result, onComplete = controller::finish)
        }
    }
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

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun FindDevicesPreview() {
    val foundDevices = MutableSharedFlow<String>()
    val characterTransfer = object: CharacterTransfer {
        override val deviceName: String = "TEST"
        override fun searchForOtherTransferDevices(): Flow<String> {
            return foundDevices
        }
        override fun close() {}
        override fun receiveCharacterFrom(
            senderName: String,
            receive: suspend (Character) -> Boolean
        ): StateFlow<CharacterTransfer.Result> {
            return MutableStateFlow(CharacterTransfer.Result.TRANSFERRING)
        }
        override fun sendCharacterToDevice(
            senderName: String,
            character: Character
        ): StateFlow<CharacterTransfer.Result> {
            return MutableStateFlow(CharacterTransfer.Result.TRANSFERRING)
        }
    }
    LaunchedEffect(true) {
        delay(250)
        foundDevices.emit("ONE")
        delay(500)
        foundDevices.emit("TWO")
        delay(500)
        foundDevices.emit("THREE")
        delay(500)
        foundDevices.emit("FOUR")
        delay(500)
        foundDevices.emit("FIVE")
    }
    FindDevices(characterTransfer) { }
}

@Composable
fun TransferResult(controller: TransferScreenController, sendOrReceive: SendOrReceive, resultStatusFlow: StateFlow<CharacterTransfer.Result>, onComplete: () -> Unit) {
    val resultStatus = remember { resultStatusFlow.value }
    when(resultStatus) {
        CharacterTransfer.Result.TRANSFERRING -> {
            throw IllegalStateException("Shouldn't be looking at result is the status is still Trasnferring")
        }
        CharacterTransfer.Result.SUCCESS -> {
            if(sendOrReceive == SendOrReceive.SEND) {
                val activeCharacter = controller.getActiveCharacter()!!
                LaunchedEffect(true) {
                    controller.deleteActiveCharacter()
                }
                val idle = activeCharacter.characterSprites.sprites[CharacterSprites.IDLE_1]
                val walk = activeCharacter.characterSprites.sprites[CharacterSprites.WALK_1]
                SendAnimation(controller, idleBitmap = idle, walkBitmap = walk) { onComplete() }
            } else {
                ReceiveAnimation(controller) { onComplete() }
            }
        }
        CharacterTransfer.Result.REJECTED -> {
            controller.endActivityWithToast("Transfer Rejected!")
        }
        CharacterTransfer.Result.FAILURE -> {
            controller.endActivityWithToast("Transfer Failed!")
        }
    }
}

interface SendAnimationController {
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val transferBackground: Bitmap
    val backgroundHeight: Dp
}

@Composable
fun SendAnimation(controller: SendAnimationController, idleBitmap: Bitmap, walkBitmap: Bitmap, onComplete: ()->Unit) {
    var targetAnimation by remember { mutableStateOf(0) }
    var idle by remember { mutableStateOf(true) }
    val flicker by animateIntAsState(targetAnimation, tween(
        durationMillis = 3000,
        easing = FastOutLinearInEasing
    )
    ) {
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
    controller.vitalBoxFactory.VitalBox {
        controller.bitmapScaler.ScaledBitmap(controller.transferBackground, "Background", alignment = Alignment.BottomCenter)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if(flicker % 2 == 0) {
                controller.bitmapScaler.ScaledBitmap(if(idle) idleBitmap else walkBitmap, "Character", alignment = Alignment.BottomCenter,
                    modifier = Modifier.offset(y = controller.backgroundHeight.times(-0.05f)))
            }
        }
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun SendAnimationPreview() {
    val context = LocalContext.current
    val imageScaler = remember { ImageScaler.getContextImageScaler(context) }
    val firmware = remember { Firmware.loadPreviewFirmwareFromDisk(context) }
    val characterSprites = remember { CardSpriteLoader.loadTestCharacterSprites(context, 3) }
    val sendAnimationController = remember {
        object: SendAnimationController {
            override val vitalBoxFactory = VitalBoxFactory(imageScaler)
            override val bitmapScaler = BitmapScaler(imageScaler)
            override val transferBackground = firmware.transformationBitmaps.rayOfLightBackground
            override val backgroundHeight = imageScaler.calculateBackgroundHeight()
        }
    }
    SendAnimation(controller = sendAnimationController, idleBitmap = characterSprites.sprites[CharacterSprites.IDLE_1], walkBitmap = characterSprites.sprites[CharacterSprites.WALK_1]) { }
}

interface ReceiveAnimationController {
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val transferBackground: Bitmap
    val backgroundHeight: Dp

    fun getLastReceivedCharacterSprites(): ReceiveCharacterSprites
}

@Composable
fun ReceiveAnimation(controller: ReceiveAnimationController, onComplete: () -> Unit) {
    val receivedCharacterSprites = remember { controller.getLastReceivedCharacterSprites() }
    val idleBitmap = receivedCharacterSprites.idle
    val happyBitmap = receivedCharacterSprites.happy
    var targetAnimation by remember { mutableStateOf(0) }
    var idle by remember { mutableStateOf(false) }
    var startIdleFlip by remember { mutableStateOf(false) }
    val flicker by animateIntAsState(targetAnimation, tween(
        durationMillis = 3000,
        easing = LinearOutSlowInEasing
    )
    ) {
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
    controller.vitalBoxFactory.VitalBox {
        controller.bitmapScaler.ScaledBitmap(controller.transferBackground, "Background", alignment = Alignment.BottomCenter)

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            if(flicker % 2 == 1) {
                controller.bitmapScaler.ScaledBitmap(if(idle) idleBitmap else happyBitmap, "Character", alignment = Alignment.BottomCenter,
                    modifier = Modifier.offset(y = controller.backgroundHeight.times(-0.05f)))
            }
        }
    }
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun ReceiveAnimationPreview() {
    val context = LocalContext.current
    val imageScaler = remember { ImageScaler.getContextImageScaler(context) }
    val firmware = remember { Firmware.loadPreviewFirmwareFromDisk(context) }
    val characterSprites = remember { CardSpriteLoader.loadTestCharacterSprites(context, 3) }
    val receiveAnimationController = remember {
        object: ReceiveAnimationController {
            override val vitalBoxFactory = VitalBoxFactory(imageScaler)
            override val bitmapScaler = BitmapScaler(imageScaler)
            override val transferBackground = firmware.transformationBitmaps.rayOfLightBackground
            override val backgroundHeight = imageScaler.calculateBackgroundHeight()
            override fun getLastReceivedCharacterSprites(): ReceiveCharacterSprites {
                return ReceiveCharacterSprites(
                    idle = characterSprites.sprites[CharacterSprites.IDLE_1],
                    happy = characterSprites.sprites[CharacterSprites.WIN])
            }
        }
    }
    ReceiveAnimation(controller = receiveAnimationController) { }
}