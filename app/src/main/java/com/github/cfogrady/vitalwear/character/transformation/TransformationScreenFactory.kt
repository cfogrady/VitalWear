package com.github.cfogrady.vitalwear.character.transformation

import android.content.Context
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.character.VBUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.VBCharacter
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.FirmwareManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransformationScreenFactory(
    private val characterManager: CharacterManager,
    private val backgroundHeight: Dp,
    private val firmwareManager: FirmwareManager,
    private val bitmapScaler: BitmapScaler,
    private val vitalBoxFactory: VitalBoxFactory,
    private val bemUpdater: VBUpdater,
) {

    companion object {
        const val TAG = "TransformationScreenFactory"
        const val PRIMARY_DELAY = 400L
    }

    enum class TransformationState {
        FUSION_PAIR,
        POWER_INCREASING,
        NEW_CHARACTER,
        SPLASH, // at this point the transformation is done
        LIGHT_OF_TRANSFORMATION,
    }

    @Composable
    fun RunTransformation(context: Context, initialCharacter: VBCharacter, onFinish: () -> Unit) {
        var character = initialCharacter
        val transformation = character.popTransformationOption()!!
        var transformationProgress by remember {
            if(transformation.fusion)
                mutableStateOf(TransformationState.FUSION_PAIR)
            else
                mutableStateOf(TransformationState.POWER_INCREASING)
        }

        val firmware by firmwareManager.getFirmware().collectAsState()
        val transformationFirmwareSprites = firmware!!.transformationFirmwareSprites
        LaunchedEffect(key1 = transformation) {
            // assume we click back and need to setup the next check
            bemUpdater.setupTransformationChecker(character)
        }

        vitalBoxFactory.VitalBox {
            when(transformationProgress) {
                TransformationState.FUSION_PAIR -> FusionPair(context, character, transformation as FusionTransformation, transformationFirmwareSprites) {
                    transformationProgress = TransformationState.NEW_CHARACTER
                }
                TransformationState.POWER_INCREASING -> PowerIncreasing(character, transformationFirmwareSprites) {
                    transformationProgress = TransformationState.NEW_CHARACTER
                }
                TransformationState.NEW_CHARACTER -> NewCharacter(
                    firmwareSprites = transformationFirmwareSprites
                ) {
                    CoroutineScope(Dispatchers.IO).launch {
                        character = characterManager.doActiveCharacterTransformation(context, transformation)
                        transformationProgress = TransformationState.SPLASH
                    }
                }
                TransformationState.SPLASH -> Splash(partner = character) {
                    transformationProgress = TransformationState.LIGHT_OF_TRANSFORMATION
                }
                TransformationState.LIGHT_OF_TRANSFORMATION -> LightOfTransformation(
                    partner = character,
                    firmwareSprites = transformationFirmwareSprites
                ) {
                    onFinish.invoke()
                }
            }
        }
    }

    @Composable
    fun FusionPair(context: Context, partner: VBCharacter, transformation: FusionTransformation, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        var fusionPhase by remember { mutableStateOf(1) }
        var newCharacterAttack by remember { mutableStateOf<Bitmap?>(null) }
        LaunchedEffect(true) {
            newCharacterAttack = characterManager.getCharacterBitmap(context, partner.cardName(), transformation.slotId, CharacterSpritesIO.ATTACK, CharacterSpritesIO.IDLE2)
        }
        when(fusionPhase) {
            1 -> FusionIdle(partner = partner, transformation = transformation, firmwareSprites = firmwareSprites) {
                fusionPhase = 2
            }
            2 -> FusionFly(partner.characterSprites.sprites[CharacterSprites.ATTACK], transformation.supportAttack, firmwareSprites) {
                fusionPhase = 3
            }
            3 -> {
                if(newCharacterAttack != null) {
                    FusionFlip(
                        partner.characterSprites.sprites[CharacterSprites.ATTACK],
                        transformation.supportAttack,
                        newCharacterAttack!!,
                        firmwareSprites
                    ) {
                        onFinish.invoke()
                    }
                } else {
                    Loading {}
                }
            }
        }

    }

    @Composable
    fun FusionIdle(partner: VBCharacter, transformation: FusionTransformation, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        var characterSprite by remember { mutableStateOf(partner.characterSprites.sprites[CharacterSprites.IDLE_1]) }
        var supportSprite by remember { mutableStateOf(transformation.supportIdle) }
        var pulseSprite by remember { mutableStateOf(firmwareSprites.weakPulse) }
        var elapsedIterations by remember { mutableStateOf(0) }
        LaunchedEffect(key1 = elapsedIterations) {
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                elapsedIterations++
                if(elapsedIterations >= 3) {
                    onFinish.invoke()
                } else if(elapsedIterations % 2 == 0) {
                    characterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_2]
                    supportSprite = transformation.supportIdle2
                    pulseSprite = firmwareSprites.strongPulse
                } else {
                    characterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_1]
                    supportSprite = transformation.supportIdle
                    pulseSprite = firmwareSprites.weakPulse
                }
            }, PRIMARY_DELAY)
        }
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.blackBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                bitmapScaler.ScaledBitmap(bitmap = pulseSprite, contentDescription = "pulse")
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.offset(x = backgroundHeight.times(-.2f))) {
                        bitmapScaler.ScaledBitmap(bitmap = supportSprite, contentDescription = "support", modifier = Modifier.graphicsLayer(scaleX = -1f))
                    }
                    Box(modifier = Modifier.offset(x = backgroundHeight.times(.2f))) {
                        bitmapScaler.ScaledBitmap(bitmap = characterSprite, contentDescription = "Character")
                    }
                }
            }
        }
    }

    @Composable
    fun FusionFly(partnerAttackSprite: Bitmap, supportAttackSprite: Bitmap, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        var horizontalTarget by remember { mutableStateOf(0.2f) }
        var pulseSprite by remember { mutableStateOf(firmwareSprites.weakPulse) }
        var lastIteration by remember { mutableStateOf(0) }
        val horizontalOffset by animateFloatAsState(
            targetValue = horizontalTarget,
            animationSpec = tween(durationMillis = PRIMARY_DELAY.toInt()*4, easing = LinearEasing),
            finishedListener = {final ->
               if(final == 0f) {
                   onFinish.invoke()
               }
            }, label = ""
        )
        LaunchedEffect(lastIteration) {
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                lastIteration++
                pulseSprite = if(lastIteration % 2 == 0) {
                    firmwareSprites.weakPulse
                } else {
                    firmwareSprites.strongPulse
                }
            }, PRIMARY_DELAY)
        }
        LaunchedEffect(true) {
            horizontalTarget = 0f
        }
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.blackBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                bitmapScaler.ScaledBitmap(bitmap = pulseSprite, contentDescription = "pulse")
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.offset(x = backgroundHeight.times(-1f * horizontalOffset))) {
                        bitmapScaler.ScaledBitmap(bitmap = supportAttackSprite, contentDescription = "support", modifier = Modifier.graphicsLayer(scaleX = -1f))
                    }
                    Box(modifier = Modifier.offset(x = backgroundHeight.times(horizontalOffset))) {
                        bitmapScaler.ScaledBitmap(bitmap = partnerAttackSprite, contentDescription = "Character")
                    }
                }
            }
        }
    }

    @Composable
    fun FusionFlip(partnerAttack: Bitmap, supportAttack: Bitmap, fusedAttack: Bitmap, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        var pulseSprite by remember { mutableStateOf(firmwareSprites.weakPulse) }
        var elapsedIterations by remember { mutableStateOf(0) }
        LaunchedEffect(elapsedIterations) {
            val delay = if(elapsedIterations < 2)
                PRIMARY_DELAY
            else if(elapsedIterations < 5)
                (PRIMARY_DELAY * .6).toLong()
            else if(elapsedIterations < 7)
                (PRIMARY_DELAY * .4).toLong()
            else 100L
            Handler.createAsync(Looper.getMainLooper()).postDelayed({
                elapsedIterations++
                if(elapsedIterations >= 15) {
                    onFinish.invoke()
                }
                pulseSprite = if(elapsedIterations % 2 == 0) {
                    firmwareSprites.weakPulse
                } else {
                    firmwareSprites.strongPulse
                }
            }, delay)
        }
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.blackBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                bitmapScaler.ScaledBitmap(bitmap = pulseSprite, contentDescription = "pulse")
                if(elapsedIterations % 2 == 0) {
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Box{
                            bitmapScaler.ScaledBitmap(bitmap = supportAttack, contentDescription = "support", modifier = Modifier.graphicsLayer(scaleX = -1f))
                        }
                        Box {
                            bitmapScaler.ScaledBitmap(bitmap = partnerAttack, contentDescription = "Character")
                        }
                    }
                } else {
                    bitmapScaler.ScaledBitmap(bitmap = fusedAttack, contentDescription = "Character")
                }
            }
        }
    }

    @Composable
    fun PowerIncreasing(partner: VBCharacter, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        /*
        Black screen with Heartbeat above idle partner
        6 iterations of weal pulse then strong pulse
        attack sprite on last iteration of strong pulse
         */
        var characterSprite by remember { mutableStateOf(partner.characterSprites.sprites[CharacterSprites.IDLE_1]) }
        var pulseSprite by remember { mutableStateOf(firmwareSprites.weakPulse) }
        var iterations by remember { mutableStateOf(0) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            iterations++
            if (iterations % 2 == 0 && iterations < 12) {
                characterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_1]
                pulseSprite = firmwareSprites.weakPulse
            } else if (iterations < 11) {
                characterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_2]
                pulseSprite = firmwareSprites.strongPulse
            } else if (iterations == 11) {
                characterSprite = if (partner.speciesStats.phase < 2) {
                    partner.characterSprites.sprites[CharacterSprites.IDLE_2]
                } else {
                    partner.characterSprites.sprites[CharacterSprites.ATTACK]
                }
                pulseSprite = firmwareSprites.strongPulse
            } else {
                onFinish.invoke()
            }
        }, PRIMARY_DELAY)
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.blackBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                bitmapScaler.ScaledBitmap(bitmap = pulseSprite, contentDescription = "pulse")
                bitmapScaler.ScaledBitmap(bitmap = characterSprite, contentDescription = "Character", alignment = Alignment.BottomCenter)
            }
        }
    }

    @Composable
    fun NewCharacter(firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        // 4 full loops
        var iterations by remember { mutableStateOf(0) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            iterations++
            if(iterations > 11) {
                onFinish.invoke()
            }
        }, PRIMARY_DELAY)
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.newBackgrounds[iterations % 3], contentDescription = "Background", alignment = Alignment.BottomCenter)
    }

    @Composable
    fun Splash(partner: VBCharacter, onFinish: () -> Unit) {
        LaunchedEffect(key1 = true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                onFinish.invoke()
            }, PRIMARY_DELAY*2)
        }
        bitmapScaler.ScaledBitmap(bitmap = partner.characterSprites.sprites[CharacterSprites.SPLASH], contentDescription = "New Partner", alignment = Alignment.BottomCenter)
    }

    @Composable
    fun LightOfTransformation(partner: VBCharacter, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        /*
        Light background with
        3 times
        Idle and Attack
         */
        var iterations by remember { mutableStateOf(0) }
        var charaterSprite by remember { mutableStateOf(partner.characterSprites.sprites[CharacterSprites.IDLE_1])}
        Handler(Looper.getMainLooper()!!).postDelayed({
            iterations++
            if (iterations > 5) {
                onFinish.invoke()
            } else if (iterations % 2 == 1) {
                charaterSprite =
                    if (partner.speciesStats.phase < 2) {
                        partner.characterSprites.sprites[CharacterSprites.IDLE_2]
                    } else {
                        partner.characterSprites.sprites[CharacterSprites.ATTACK]
                    }
            } else  {
                charaterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_1]
            }
        }, PRIMARY_DELAY)
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.rayOfLightBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(bitmap = charaterSprite, contentDescription = "Character", alignment = Alignment.BottomCenter, modifier = Modifier.offset(y = backgroundHeight.times(-.05f)))
        }

    }
}