package com.github.cfogrady.vitalwear.companion.card

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.github.cfogrady.vitalwear.common.card.ValidatedCardManager
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import com.github.cfogrady.vitalwear.companion.VitalWearCompanion
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.NodeClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
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
    private lateinit var uri: Uri
    private lateinit var cardName: String
    private var uniqueSprites = false
    private lateinit var card: Card<out DimHeader, out CharacterStats<out CharacterStatsEntry>, out TransformationRequirements<out TransformationRequirementsEntry>, out AdventureLevels<out AdventureLevel>, out AttributeFusions, out SpecificFusions<out SpecificFusionEntry>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        validatedCardManager = (applicationContext as VitalWearCompanion).validatedCardManager
        cardLoader = (applicationContext as VitalWearCompanion).cardLoader
        filePickLauncher = buildFilePickLauncher()
        cardVerificationLauncher = buildCardVerificationLauncher()
        setContent {
            val state by importState.collectAsState()
            when(state) {
                ImportState.PickFile -> {
                    filePickLauncher.launch(arrayOf("*/*"))
                }
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
                        loadCard()
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

    private fun buildFilePickLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            if(it == null) {
                finish()
            } else {
                uri = it
                Log.i(TAG, "Path: ${uri.path}")
                val path = uri.path!!
                cardName = path.substring(path.lastIndexOf("/")+1)
                if(cardName.contains(".")) {
                    cardName = cardName.substring(0, cardName.lastIndexOf("."))
                }
                Log.i(TAG, "Card: $cardName")
                importState.value = ImportState.LoadFile
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
                validatedCardManager.addValidatedCard(validatedCardId)
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
        cardLoader.importCardImage(applicationContext, "", card, false)
        val channelClient = Wearable.getChannelClient(this)
        val nodes = Wearable.getNodeClient(this).connectedNodes.await()
        for (node in nodes) {
            val channel = channelClient.openChannel(node.id, ChannelTypes.CARD_DATA).await()
            channelClient.getOutputStream(channel).await().use {os ->
                val cardWriter = DimWriter()
                os.write(cardName.toByteArray(Charset.defaultCharset()))
                os.write(0)
                os.write(if(uniqueSprites) 1 else 0)
                cardWriter.writeCard(card, os)
            }
            channelClient.close(channel).await()
        }
    }
}