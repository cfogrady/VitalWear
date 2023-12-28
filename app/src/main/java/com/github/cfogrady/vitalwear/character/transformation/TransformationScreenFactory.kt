package com.github.cfogrady.vitalwear.character.transformation

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.github.cfogrady.vitalwear.character.BEMUpdater
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.character.data.BEMCharacter
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.FirmwareManager

class TransformationScreenFactory(
    private val characterManager: CharacterManager,
    private val backgroundHeight: Dp,
    private val firmwareManager: FirmwareManager,
    private val bitmapScaler: BitmapScaler,
    private val vitalBoxFactory: VitalBoxFactory,
    private val bemUpdater: BEMUpdater,
) {
    enum class TransformationState {
        POWER_INCREASING,
        NEW_CHARACTER,
        SPLASH, // at this point the transformation is done
        LIGHT_OF_TRANSFORMATION,
    }

    @Composable
    fun RunTransformation(context: Context, onFinish: () -> Unit) {
        var transformationProgress by remember { mutableStateOf(TransformationState.POWER_INCREASING) }
        var character = characterManager.getCurrentCharacter()
        val transformationFirmwareSprites = firmwareManager.getFirmware().value!!.transformationFirmwareSprites
        val transformationOption = remember { character.popTransformationOption().get() }
        LaunchedEffect(key1 = transformationOption) {
            // assume we click back and need to setup the next check
            bemUpdater.setupTransformationChecker(character)
        }

        vitalBoxFactory.VitalBox {
            when(transformationProgress) {
                TransformationState.POWER_INCREASING -> PowerIncreasing(character, transformationFirmwareSprites) {
                    transformationProgress = TransformationState.NEW_CHARACTER
                }
                TransformationState.NEW_CHARACTER -> NewCharacter(
                    firmwareSprites = transformationFirmwareSprites
                ) {
                    character = characterManager.doActiveCharacterTransformation(context, transformationOption)
                    transformationProgress = TransformationState.SPLASH
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
    fun PowerIncreasing(partner: BEMCharacter, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
        /*
        Black screen with Heartbeat above idle partner
        6 iterations of weal pulse then strong pulse
        attack sprite on last iteration of strong pulse
         */
        var charaterSprite by remember { mutableStateOf(partner.characterSprites.sprites[CharacterSprites.IDLE_1]) }
        var pulseSprite by remember { mutableStateOf(firmwareSprites.weakPulse) }
        var iterations by remember { mutableStateOf(0) }
        Handler(Looper.getMainLooper()!!).postDelayed({
            iterations++
            if (iterations % 2 == 0 && iterations < 12) {
                charaterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_1]
                pulseSprite = firmwareSprites.weakPulse
            } else if (iterations < 11) {
                charaterSprite = partner.characterSprites.sprites[CharacterSprites.IDLE_2]
                pulseSprite = firmwareSprites.strongPulse
            } else if (iterations == 11) {
                charaterSprite = if (partner.speciesStats.phase < 2) {
                    partner.characterSprites.sprites[CharacterSprites.IDLE_2]
                } else {
                    partner.characterSprites.sprites[CharacterSprites.ATTACK]
                }
                pulseSprite = firmwareSprites.strongPulse
            } else {
                onFinish.invoke()
            }
        }, 500)
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.blackBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
                .fillMaxWidth()
                .offset(y = backgroundHeight.times(-.05f))) {
                bitmapScaler.ScaledBitmap(bitmap = pulseSprite, contentDescription = "pulse")
                bitmapScaler.ScaledBitmap(bitmap = charaterSprite, contentDescription = "Character", alignment = Alignment.BottomCenter)
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
        }, 500)
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.newBackgrounds[iterations % 3], contentDescription = "Background", alignment = Alignment.BottomCenter)
    }

    @Composable
    fun Splash(partner: BEMCharacter, onFinish: () -> Unit) {
        LaunchedEffect(key1 = true) {
            Handler(Looper.getMainLooper()!!).postDelayed({
                onFinish.invoke()
            }, 1000)
        }
        bitmapScaler.ScaledBitmap(bitmap = partner.characterSprites.sprites[CharacterSprites.SPLASH], contentDescription = "New Partner", alignment = Alignment.BottomCenter)
    }

    @Composable
    fun LightOfTransformation(partner: BEMCharacter, firmwareSprites: TransformationFirmwareSprites, onFinish: () -> Unit) {
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
        }, 500)
        bitmapScaler.ScaledBitmap(bitmap = firmwareSprites.rayOfLightBackground, contentDescription = "Background", alignment = Alignment.BottomCenter)
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            bitmapScaler.ScaledBitmap(bitmap = charaterSprite, contentDescription = "Character", alignment = Alignment.BottomCenter, modifier = Modifier.offset(y = backgroundHeight.times(-.05f)))
//            Column(verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier
//                .fillMaxWidth()
//                .offset(y = backgroundHeight.times(-.05f))) {
//                bitmapScaler.ScaledBitmap(bitmap = pulseSprite, contentDescription = "pulse")
//                bitmapScaler.ScaledBitmap(bitmap = charaterSprite, contentDescription = "Character", alignment = Alignment.BottomCenter)
//            }
        }

    }
}