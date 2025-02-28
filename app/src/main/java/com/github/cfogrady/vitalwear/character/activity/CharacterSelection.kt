package com.github.cfogrady.vitalwear.character.activity

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.character.data.CharacterPreview
import com.github.cfogrady.vitalwear.character.data.CharacterState
import com.github.cfogrady.vitalwear.common.card.CardSpriteLoader
import com.github.cfogrady.vitalwear.common.character.CharacterSprites
import com.github.cfogrady.vitalwear.composable.util.BitmapScaler
import com.github.cfogrady.vitalwear.composable.util.ImageScaler
import com.github.cfogrady.vitalwear.composable.util.VitalBoxFactory
import com.github.cfogrady.vitalwear.firmware.Firmware
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

interface CharacterSelectionController: PagedCharacterSelectionMenuController {
    val loadingNewCharacterState: StateFlow<Boolean>
    fun getPreviewCharacters() : List<CharacterPreview>
}

@Composable
fun CharacterSelection(controller: CharacterSelectionController) {
    var loaded by remember { mutableStateOf(false) }
    var characters by remember { mutableStateOf(ArrayList<CharacterPreview>() as List<CharacterPreview>) }
    val loadingNewCharacter by controller.loadingNewCharacterState.collectAsState()
    if(loadingNewCharacter) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Setting Up New Character")
        }
    } else if(!loaded) {
        Loading() {
            characters = controller.getPreviewCharacters()
            loaded = true
        }
    } else {
        PagedCharacterSelectionMenu(characters = characters, controller)
    }
}

interface PagedCharacterSelectionMenuController: PreviewCharacterController {
    fun newCharacter()
    val backgroundFlow: StateFlow<Bitmap>
    fun setSupportCharacter(character: CharacterPreview)
    fun deleteCharacter(character: CharacterPreview)
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PagedCharacterSelectionMenu(characters: List<CharacterPreview>, controller: PagedCharacterSelectionMenuController) {
    val options = characters.toMutableStateList()
    val background by controller.backgroundFlow.collectAsState()
    controller.vitalBoxFactory.VitalBox {
        controller.bitmapScaler.ScaledBitmap(
            bitmap = background,
            contentDescription = "background"
        )
        val pagerState = rememberPagerState(pageCount = {options.size + 1})
        VerticalPager(state = pagerState) {
            if(it == 0) {
                Box(modifier = Modifier
                    .fillMaxSize()
                    .clickable(onClick = controller::newCharacter)) {
                    Text(text = "NEW", fontSize = 6.em, fontWeight = FontWeight.Bold, modifier = Modifier.align(
                        Alignment.Center))
                }
            } else {
                val character = options[it - 1]
                PreviewCharacter(controller, character = character, onSetSupport =  {
                    controller.setSupportCharacter(character)
                    for( i in 0 until options.size) {
                        if(options[i].state == CharacterState.SUPPORT) {
                            options[i] = CharacterPreview(options[i].cardName, options[i].slotId, options[i].characterId, CharacterState.STORED, options[i].idle)
                        }
                    }
                    options[it - 1] = CharacterPreview(character.cardName, character.slotId, character.characterId, CharacterState.SUPPORT, character.idle)
                }, onDelete = {
                    options.removeAt(it-1)
                    controller.deleteCharacter(character)
                })
            }
        }
    }
}

interface PreviewCharacterController {
    fun swapToCharacter(character: CharacterPreview)
    val vitalBoxFactory: VitalBoxFactory
    val bitmapScaler: BitmapScaler
    val supportIcon: Bitmap
}

@Composable
private fun PreviewCharacter(controller: PreviewCharacterController, character: CharacterPreview, onSetSupport: ()->Unit, onDelete: ()->Unit) {
    var showMenu by remember { mutableStateOf(false) }
    if(showMenu) {
        Column(modifier = Modifier
            .fillMaxSize()
            .clickable { showMenu = false }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceAround) {
            Button(onClick = {
                controller.swapToCharacter(character)
            }) {
                Text(text = "Select", Modifier.padding(10.dp))
            }
            Button(onClick = {
                onSetSupport.invoke()
                showMenu = false
            }) {
                Text(text = "Support", Modifier.padding(10.dp))
            }
            Button(onClick = {
                onDelete.invoke()
                showMenu = false
            }) {
                Text(text = "Delete", Modifier.padding(10.dp))
            }
        }
    } else {
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .combinedClickable(onLongClick = {
                    showMenu = true
                }, onClick = {
                    controller.swapToCharacter(character)
                })) {
            controller.bitmapScaler.ScaledBitmap(
                bitmap = character.idle,
                contentDescription = "Character"
            )
            if(character.state == CharacterState.SUPPORT) {
                controller.bitmapScaler.ScaledBitmap(
                    bitmap = controller.supportIcon,
                    contentDescription = "Support"
                )
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
private fun PreviewPagedCharacterSelectionMenu() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(LocalContext.current, 2)
    val characterSprites2: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(LocalContext.current, 3)
    val background: Bitmap = BitmapFactory.decodeStream(LocalContext.current.assets.open("test_background.png"))
    val characterPreview = CharacterPreview(
        cardName = "TestCard",
        slotId = 2,
        characterId = 0,
        state = CharacterState.STORED,
        idle = characterSprites.sprites[CharacterSprites.IDLE_1]
    )
    val characterPreview2 = CharacterPreview(
        cardName = "TestCard",
        slotId = 3,
        characterId = 1,
        state = CharacterState.SUPPORT,
        idle = characterSprites2.sprites[CharacterSprites.IDLE_1]
    )
    val characterPreviews = mutableListOf(characterPreview, characterPreview2)
    val controller = object: PagedCharacterSelectionMenuController{
        override fun newCharacter() {}
        override val backgroundFlow: StateFlow<Bitmap> = MutableStateFlow(background)
        override fun setSupportCharacter(character: CharacterPreview) {}
        override fun deleteCharacter(character: CharacterPreview) {
            characterPreviews.remove(character)
        }

        override fun swapToCharacter(character: CharacterPreview) {}
        override val vitalBoxFactory: VitalBoxFactory = vitalBoxFactory
        override val bitmapScaler: BitmapScaler = bitmapScaler
        override val supportIcon: Bitmap = firmware.characterFirmwareSprites.supportIcon
    }
    PagedCharacterSelectionMenu(characterPreviews, controller)
}

@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun PreviewPreviewCharacter() {
    val imageScaler = ImageScaler.getContextImageScaler(LocalContext.current)
    val bitmapScaler = BitmapScaler(imageScaler)
    val vitalBoxFactory = VitalBoxFactory(imageScaler)
    val firmware = Firmware.loadPreviewFirmwareFromDisk(LocalContext.current)
    val characterSprites: CharacterSprites = CardSpriteLoader.loadTestCharacterSprites(LocalContext.current, 2)
    // background: Bitmap = BitmapFactory.decodeStream(context.assets.open("test_background.png")),
    val controller = object: PreviewCharacterController{
        override fun swapToCharacter(character: CharacterPreview) {}
        override val vitalBoxFactory: VitalBoxFactory = vitalBoxFactory
        override val bitmapScaler: BitmapScaler = bitmapScaler
        override val supportIcon: Bitmap = firmware.characterFirmwareSprites.supportIcon
    }
    val characterPreview = CharacterPreview(
        cardName = "TestCard",
        slotId = 2,
        characterId = 0,
        state = CharacterState.STORED,
        idle = characterSprites.sprites[CharacterSprites.IDLE_1]
    )
    PreviewCharacter(controller, characterPreview, {}, {})
}