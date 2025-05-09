package com.github.cfogrady.vitalwear.character.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.github.cfogrady.vitalwear.common.card.CardType
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

interface CardSelectController {
    val cardsImported: StateFlow<Int>
    fun loadCards(): List<CardMetaEntity>
}

@Composable
fun CardSelection(controller: CardSelectController, onSelect: (CardMetaEntity) -> Unit) {
    val cardsImported by controller.cardsImported.collectAsState()
    var loaded by remember { mutableStateOf(false) }
    var cards by remember { mutableStateOf(ArrayList<CardMetaEntity>() as List<CardMetaEntity>) }
    LaunchedEffect(cardsImported) {
        loaded = false
        withContext(Dispatchers.IO) {
            cards = controller.loadCards()
            loaded = true
        }
    }
    if(!loaded) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = LOADING_TEXT)
        }
    } else if(cards.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Import Card From Phone")
        }
    } else {
        LoadedCardSelection(cards, onSelect = {
            onSelect(it)
        })
    }
}

@Composable
fun LoadedCardSelection(cards: List<CardMetaEntity>, onSelect: (CardMetaEntity)->Unit) {
    ScalingLazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        items(items = cards) { card ->
            Button(onClick = {
                onSelect(card)
            }) {
                Text(text = card.cardName, modifier = Modifier.padding(10.dp))
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
private fun PreviewLoadedCardSelection() {
    LoadedCardSelection(listOf(
        CardMetaEntity(
            cardName = "Card 1",
            cardId = 0,
            cardType = CardType.DIM,
            cardChecksum = 0,
            franchise = 0,
            maxAdventureCompletion = null,
        ),
        CardMetaEntity(
            cardName = "Medium Card",
            cardId = 1,
            cardType = CardType.DIM,
            cardChecksum = 0,
            franchise = 0,
            maxAdventureCompletion = null,
        ),
        CardMetaEntity(
            cardName = "Card With Long Name",
            cardId = 2,
            cardType = CardType.BEM,
            cardChecksum = 0,
            franchise = 1,
            maxAdventureCompletion = null,
        ),
    ), onSelect = {})
}


@Preview(
    device = WearDevices.LARGE_ROUND,
    showSystemUi = true,
    backgroundColor = 0xff000000,
    showBackground = true
)
@Composable
private fun PreviewCardSelection() {
    CardSelection(object: CardSelectController{
        override val cardsImported: StateFlow<Int>
            get() = MutableStateFlow(0)

        override fun loadCards(): List<CardMetaEntity> {
            return listOf(
                CardMetaEntity(
                    cardName = "Testmon",
                    cardId = 0,
                    cardChecksum = 42,
                    cardType = CardType.DIM,
                    franchise = 0,
                    maxAdventureCompletion = null
                ),
                CardMetaEntity(
                    cardName = "ValidateMon",
                    cardId = 1,
                    cardChecksum = 69,
                    cardType = CardType.BEM,
                    franchise = 1,
                    maxAdventureCompletion = null
                ),
            )
        }
    }, onSelect = {})
}