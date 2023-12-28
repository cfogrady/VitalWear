package com.github.cfogrady.vitalwear.card

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Checkbox
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import com.github.cfogrady.vb.dim.adventure.AdventureLevels
import com.github.cfogrady.vb.dim.adventure.AdventureLevels.AdventureLevel
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.card.DimWriter
import com.github.cfogrady.vb.dim.character.CharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats.CharacterStatsEntry
import com.github.cfogrady.vb.dim.fusion.AttributeFusions
import com.github.cfogrady.vb.dim.fusion.SpecificFusions
import com.github.cfogrady.vb.dim.fusion.SpecificFusions.SpecificFusionEntry
import com.github.cfogrady.vb.dim.header.DimHeader
import com.github.cfogrady.vb.dim.transformation.TransformationRequirements
import com.github.cfogrady.vb.dim.transformation.TransformationRequirements.TransformationRequirementsEntry
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.common.card.CardLoader
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.VitalWearCompanion
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.nio.charset.Charset

/**
 * Activity to import card images
 */
class ImportCardActivity() : ComponentActivity() {

    private val activityHelper: ActivityHelper = ActivityHelper(this)

    companion object {
        const val TAG = "ImportCardActivity"
    }

    enum class ImportState {
        PickFile,
        NameOrUnique,
        LoadFile,
        UnlockCard,
        ImportCard,
        Success
    }

    private lateinit var filePickLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cardVerificationLauncher: ((Intent)->Unit)->Unit
    private lateinit var validatedCardManager: ValidatedCardManager
    private lateinit var cardLoader: CardLoader

    private val importState = MutableStateFlow(ImportState.PickFile)
    private var cardsLoaded = setOf<String>()
    private lateinit var uri: Uri
    private var cardName = MutableStateFlow("")
    private var uniqueSprites = MutableStateFlow(false)
    private lateinit var card: Card<out DimHeader, out CharacterStats<out CharacterStatsEntry>, out TransformationRequirements<out TransformationRequirementsEntry>, out AdventureLevels<out AdventureLevel>, out AttributeFusions, out SpecificFusions<out SpecificFusionEntry>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validatedCardManager = (applicationContext as VitalWearCompanion).validatedCardManager
        cardLoader = (applicationContext as VitalWearCompanion).cardLoader
        val cardMetaEntityDao = (applicationContext as VitalWearCompanion).cardMetaEntityDao
        filePickLauncher = buildFilePickLauncher()
        cardVerificationLauncher = buildCardVerificationLauncher()
        setContent {
            LaunchedEffect(true) {
                withContext(Dispatchers.IO) {
                    cardsLoaded = cardMetaEntityDao.getAll().map{it.cardName}.toSet()
                }
            }
            KeepScreenOn()
            val state by importState.collectAsState()
            when(state) {
                ImportState.PickFile -> {
                    filePickLauncher.launch(arrayOf("*/*"))
                }
                ImportState.NameOrUnique -> NameOrUnique()
                ImportState.LoadFile -> {
                    Loading(loadingText = "Loading Card Image") {
                        loadCard()
                    }
                }
                ImportState.UnlockCard -> {
                    cardVerificationLauncher.invoke {
                        Log.i(TAG, "Adding cardId to be validated: ${card.header.dimId}")
                        it.putExtra(ValidateCardActivity.CARD_VALIDATED_KEY, card.header.dimId)
                    }
                }
                ImportState.ImportCard -> {
                    LaunchedEffect(true) {
                        withContext(Dispatchers.IO) {
                            importCard()
                            importState.value = ImportState.Success
                        }

                    }
                    Loading(loadingText = "Importing Card Image") {
                    }
                }
                ImportState.Success -> {
                    LaunchedEffect(true) {
                        Handler(Looper.getMainLooper()!!).postDelayed({
                            finish()
                        }, 1000)
                    }
                    Text(text = "Imported Successfully")
                }
            }

        }
    }

    @Composable
    @Preview
    private fun NameOrUnique() {
        val filePath = uri.path!!
        val name by cardName.collectAsState()
        val unique by uniqueSprites.collectAsState()
        Box(Modifier.background(Color(0, 150, 0))) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Button(modifier = Modifier.padding(10.dp), onClick = { importState.value = ImportState.PickFile }) {
                        Text(text = "File")
                    }
                    Text(text = filePath)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Name: ", modifier = Modifier.padding(10.dp), fontSize = 5.em, fontWeight = FontWeight.Bold)
                    TextField(value = name, onValueChange = {
                        cardName.value = it
                    })
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Unique Sprites:", fontSize = 5.em, fontWeight = FontWeight.Bold)
                    Checkbox(checked = unique, onCheckedChange = {uniqueSprites.value = it})
                }
                Button(onClick = {
                    if(cardsLoaded.contains(name)) {
                        Toast.makeText(applicationContext, "Card named \"$name\" has already been imported", Toast.LENGTH_SHORT).show()
                    } else {
                        importState.value = ImportState.LoadFile
                    }
                }) {
                    Text(text = "Import")
                }
            }
        }
    }

    private fun buildFilePickLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if(it == null) {
                finish()
            } else {
                uri = it
                Log.i(TAG, "Path: ${uri.path}")
                val path = uri.path!!
                var name = path.substring(path.lastIndexOf("/")+1)
                if(name.contains(".")) {
                    name = name.substring(0, name.lastIndexOf("."))
                }
                cardName.value = name
                Log.i(TAG, "Card: $name")
                importState.value = ImportState.NameOrUnique
            }
        }
    }

    private fun buildCardVerificationLauncher(): ((Intent)->Unit)->Unit {
        return activityHelper.getActivityLauncherWithResultHandling(ValidateCardActivity::class.java) {
            val validatedCardId = it.data?.extras?.getInt(ValidateCardActivity.CARD_VALIDATED_KEY, -1)
            if(validatedCardId == null) {
                Log.i(TAG, "Didn't receive cardId")
                finish()
            } else {
                Log.i(TAG, "Validated Card $validatedCardId")
                CoroutineScope(Dispatchers.IO).launch {
                    validatedCardManager.addValidatedCard(validatedCardId)
                }
                importState.value = ImportState.ImportCard
            }
        }
    }

    private fun loadCard() {
        val dimReader = DimReader()
        contentResolver.openInputStream(uri).use {
            card = dimReader.readCard(it, false)
            if (validatedCardManager.isValidatedCard(card.header.dimId)) {
                importState.value = ImportState.ImportCard
            } else {
                importState.value = ImportState.UnlockCard
            }
        }
    }

    private suspend fun importCard() {
        // cardLoader.importCardImage(applicationContext, "", card, false)
        val channelClient = Wearable.getChannelClient(this)
        val nodes = Wearable.getNodeClient(this).connectedNodes.await()
        for (node in nodes) {
            val channel = channelClient.openChannel(node.id, ChannelTypes.CARD_DATA).await()
            Log.i(TAG, "Digiport open!")
            channelClient.getOutputStream(channel).await().use {os ->
                Log.i(TAG, "Writing the card data!")
                val cardWriter = DimWriter()
                os.write(cardName.value.toByteArray(Charset.defaultCharset()))
                os.write(0)
                os.write("TEST".toByteArray(Charset.defaultCharset()))
                os.write(0)
                os.write(if(uniqueSprites.value) 1 else 0)
                cardWriter.writeCard(card, os)
            }
            channelClient.close(channel).await()
        }
    }
}