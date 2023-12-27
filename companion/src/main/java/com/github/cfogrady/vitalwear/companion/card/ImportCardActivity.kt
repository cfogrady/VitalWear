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
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.integerArrayResource
import com.github.cfogrady.vb.dim.adventure.AdventureLevels
import com.github.cfogrady.vb.dim.adventure.AdventureLevels.AdventureLevel
import com.github.cfogrady.vb.dim.card.Card
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vb.dim.character.CharacterStats
import com.github.cfogrady.vb.dim.character.CharacterStats.CharacterStatsEntry
import com.github.cfogrady.vb.dim.fusion.AttributeFusions
import com.github.cfogrady.vb.dim.fusion.SpecificFusions
import com.github.cfogrady.vb.dim.fusion.SpecificFusions.SpecificFusionEntry
import com.github.cfogrady.vb.dim.header.DimHeader
import com.github.cfogrady.vb.dim.transformation.TransformationRequirements
import com.github.cfogrady.vb.dim.transformation.TransformationRequirements.TransformationRequirementsEntry
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.common.util.ActivityHelper
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import java.io.FileInputStream

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
        Success
    }

    private lateinit var filePickLauncher: ActivityResultLauncher<Array<String>>
    private lateinit var cardVerificationLauncher: ((Intent)->Unit)->Unit

    private val importState = MutableStateFlow(ImportState.PickFile)
    private lateinit var uri: Uri
    private lateinit var cardFile: File
    private lateinit var card: Card<out DimHeader, out CharacterStats<out CharacterStatsEntry>, out TransformationRequirements<out TransformationRequirementsEntry>, out AdventureLevels<out AdventureLevel>, out AttributeFusions, out SpecificFusions<out SpecificFusionEntry>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                        importCard()
                    }
                }
                ImportState.UnlockCard -> {
                    cardVerificationLauncher.invoke {
                        Log.i(TAG, "Adding cardId to be validated: ${card.header.dimId}")
                        it.putExtra(ValidateCardActivity.CARD_VALIDATED_KEY, card.header.dimId)
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
                cardFile = File(it.path!!)
                importState.value = ImportState.LoadFile
            }
        }
    }

    private fun buildCardVerificationLauncher(): ((Intent)->Unit)->Unit {
        return activityHelper.getActivityLauncherWithResultHandling(ValidateCardActivity::class.java) {
            val validatedCardId = it.data?.extras?.getInt(ValidateCardActivity.CARD_VALIDATED_KEY, -1)
            Log.i(TAG, "Validated Card $validatedCardId")
            importState.value = ImportState.Success
        }
    }

    private fun importCard() {
        val dimReader = DimReader()
        contentResolver.openInputStream(uri).use {
            card = dimReader.readCard(it, false)
            importState.value = ImportState.UnlockCard
        }
    }
}