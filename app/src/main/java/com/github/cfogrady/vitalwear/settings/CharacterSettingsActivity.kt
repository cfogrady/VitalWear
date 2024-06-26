package com.github.cfogrady.vitalwear.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Checkbox
import androidx.wear.compose.material.RadioButton
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.common.card.CardType
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

class CharacterSettingsActivity : ComponentActivity() {
    companion object {
        const val CARD_TYPE = "CardType"
        const val CHARACTER_SETTINGS = "CharacterSettings"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.i("Configure Settings")
        val cardType = intent.getSerializableExtra(CARD_TYPE) as CardType
        setContent {
            BuildScreen(cardType)
        }
    }

    @Composable
    private fun BuildScreen(cardType: CardType) {
        var franchises by remember { mutableStateOf(emptyList<Int>()) }
        var loading by remember { mutableStateOf(true) }
        if(loading) {
            Loading(scope = Dispatchers.IO) {
                if(cardType == CardType.DIM) {
                    franchises = (application as VitalWearApp).cardMetaEntityDao.getNonDIMFranchises()
                    loading = false
                } else {
                    loading = false
                }
            }
        } else {
            SettingsControls(cardType = cardType, franchises = franchises)
        }


    }

    @Composable
    private fun SettingsControls(cardType: CardType, franchises: List<Int>) {
        var trainInBackground by remember { mutableStateOf(true) }
        var allowedBattles by remember { mutableStateOf(CharacterSettings.AllowedBattles.CARD_ONLY) }
        var assumedFranchise by remember { mutableStateOf<Int?>(null) }
        ScalingLazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally){
            item {
                // Take some space at the top so the Background training isn't cut off
                Box(modifier = Modifier.height(2.dp))
            }
            item {
                Row {
                    Text(text = "Background Training:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                    Checkbox(checked = trainInBackground, onCheckedChange = {trainInBackground = it})
                }
            }
            if(cardType == CardType.DIM && franchises.isNotEmpty()) {
                item {
                    Column {
                        Divider(thickness = 1.dp)
                        Row {
                            Text(text = "Scale DIM to BEM:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                            Checkbox(checked = assumedFranchise != null, onCheckedChange = {
                                if(it) {
                                    assumedFranchise = 1
                                } else {
                                    assumedFranchise = null
                                    if(!allowedBattles.showOnDIM) {
                                        allowedBattles = CharacterSettings.AllowedBattles.CARD_ONLY
                                    }
                                }
                            })
                        }
                        if(assumedFranchise != null) {
                            Text(text = "Franchises: ", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                            franchises.forEach{
                                Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.clickable {
                                    assumedFranchise = it
                                }) {
                                    RadioButton(
                                        selected = assumedFranchise == it,
                                        modifier = Modifier.weight(.2f))
                                    Text(text = "$it", modifier = Modifier.weight(.8f))
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column {
                    Divider(thickness = 1.dp)
                    Text(text = "Battle Options:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(5.dp))
                    CharacterSettings.AllowedBattles.entries.filter { (cardType == CardType.BEM || assumedFranchise != null) || it.showOnDIM }.forEach{ allowBattlesOption ->
                        Row(horizontalArrangement = Arrangement.Start, modifier = Modifier.clickable {
                            allowedBattles = allowBattlesOption
                        }) {
                            RadioButton(
                                selected = allowedBattles == allowBattlesOption,
                                modifier = Modifier.weight(.2f))
                            Text(text = allowBattlesOption.descr, modifier = Modifier.weight(.8f))
                        }
                    }
                }
            }
            item {
                Button(onClick = {
                    val intent = Intent()
                    val characterSettings = CharacterSettings(0, trainInBackground, allowedBattles, assumedFranchise)
                    intent.putExtra(CHARACTER_SETTINGS, characterSettings)
                    setResult(0, intent)
                    finish()
                }) {
                    Text(text = "Continue", modifier = Modifier.padding(5.dp))
                }
            }
        }
    }

}