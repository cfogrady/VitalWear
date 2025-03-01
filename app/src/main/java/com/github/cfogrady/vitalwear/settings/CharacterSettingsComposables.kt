package com.github.cfogrady.vitalwear.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.CheckboxButton
import androidx.wear.compose.material3.RadioButton
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.common.card.CardType
import kotlinx.coroutines.Dispatchers

interface CharacterSettingsController: SettingsControlsController {
    suspend fun getNonDIMFranchises(): List<Int>
}

@Composable
fun CharacterSettingsScreen(controller: CharacterSettingsController, cardType: CardType) {
    var franchises by remember { mutableStateOf(emptyList<Int>()) }
    var loading by remember { mutableStateOf(true) }
    if(loading) {
        Loading(scope = Dispatchers.IO) {
            if(cardType == CardType.DIM) {
                franchises = controller.getNonDIMFranchises()
                loading = false
            } else {
                loading = false
            }
        }
    } else {
        SettingsControls(controller = controller, cardType = cardType, franchises = franchises)
    }
}

interface SettingsControlsController {
    fun finishSettings(characterSettings: CharacterSettings)
}

@Composable
private fun SettingsControls(controller: SettingsControlsController, cardType: CardType, franchises: List<Int>) {
    var trainInBackground by remember { mutableStateOf(true) }
    var allowedBattles by remember { mutableStateOf(CharacterSettings.AllowedBattles.CARD_ONLY) }
    var assumedFranchise by remember { mutableStateOf<Int?>(null) }
    ScalingLazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
        item {
            Box(modifier = Modifier.height(2.dp))
        }
        item {
            CheckboxButton(modifier = Modifier.padding(5.dp), checked = trainInBackground, onCheckedChange = {trainInBackground = it}) {
                Text(text = "Background Training:", fontWeight = FontWeight.Bold)
            }
        }
        if(cardType == CardType.DIM && franchises.isNotEmpty()) {
            item {
                Column {
                    HorizontalDivider(thickness = 1.dp)
                    CheckboxButton(modifier = Modifier.padding(5.dp), checked = assumedFranchise != null, onCheckedChange = {
                        if(it) {
                            assumedFranchise = 1
                        } else {
                            assumedFranchise = null
                            if(!allowedBattles.showOnDIM) {
                                allowedBattles = CharacterSettings.AllowedBattles.CARD_ONLY
                            }
                        }
                    }) {
                        Text(text = "Scale DIM to BEM:", fontWeight = FontWeight.Bold)
                    }
                    if(assumedFranchise != null) {
                        Text(text = "Franchises: ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                        franchises.forEach{
                            Row(modifier = Modifier.padding(5.dp)) {
                                RadioButton(
                                    selected = assumedFranchise == it,
                                    onSelect = {assumedFranchise = it},
                                ) {
                                    Text(text = "$it")
                                }
                            }
                        }
                    }
                }
            }
        }
        item {
            Column {
                HorizontalDivider(thickness = 1.dp)
                Text(text = "Random Battle Opponents:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                CharacterSettings.AllowedBattles.entries.filter { (cardType == CardType.BEM || assumedFranchise != null) || it.showOnDIM }.forEach{ allowBattlesOption ->
                    Row(modifier = Modifier.padding(5.dp)) {
                        RadioButton(
                            selected = allowedBattles == allowBattlesOption,
                            onSelect = {allowedBattles = allowBattlesOption},
                        ) {
                            Text(text = allowBattlesOption.descr)
                        }
                    }
                }
            }
        }
        item {
            Button(onClick = {
                val characterSettings = CharacterSettings(0, trainInBackground, allowedBattles, assumedFranchise)
                controller.finishSettings(characterSettings)
            }) {
                Text(text = "Continue", modifier = Modifier.padding(5.dp))
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
private fun PreviewSettingsControls() {
    val controller = object: CharacterSettingsController {
        override suspend fun getNonDIMFranchises(): List<Int> {
            return listOf(0, 1, 2, 3)
        }

        override fun finishSettings(characterSettings: CharacterSettings) {}
    }
    CharacterSettingsScreen(controller, CardType.BEM)
}