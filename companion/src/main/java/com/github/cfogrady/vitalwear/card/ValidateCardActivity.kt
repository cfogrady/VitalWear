package com.github.cfogrady.vitalwear.card

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.cfogrady.vitalwear.Loading
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

/**
 * This activity is used to connect with a VBBE to validate cardIds.
 */
class ValidateCardActivity : ComponentActivity(), NfcAdapter.ReaderCallback {

    companion object {
        const val CARD_VALIDATED_KEY = "CARD_VALIDATED"
    }

    enum class CardValidationState {
        WaitingForVBConnect,
        ValidateCardOnVB,
        Success,
    }

    private lateinit var nfcAdapter: NfcAdapter
    private var validationStateFlow = MutableStateFlow(CardValidationState.WaitingForVBConnect)
    private var cardIdToValidate: UShort = 0u

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val inputCardId = intent.getIntExtra(CARD_VALIDATED_KEY, -1)
        if(inputCardId == -1) {
            Timber.e("ValidateCardActivity called without card number!")
            finish()
            return
        }
        cardIdToValidate = inputCardId.toUShort()
        if (maybeNfcAdapter == null) {
            Toast.makeText(this, "No NFC on device!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        nfcAdapter = maybeNfcAdapter
        setContent {
            DisplayContent()
        }
    }

    @Composable
    fun DisplayContent() {
        val validationState by validationStateFlow.collectAsState()
        when(validationState) {
            CardValidationState.WaitingForVBConnect -> {
                Loading(loadingText = "Connect to VB") {}
            }
            CardValidationState.ValidateCardOnVB -> {
                Loading(loadingText = "Insert Card in VB and Reconnect") {}
            }
            CardValidationState.Success -> {
                LaunchedEffect(true) {
                    Handler(Looper.getMainLooper()!!).postDelayed({
                        val intent = Intent()
                        intent.putExtra(CARD_VALIDATED_KEY, cardIdToValidate.toInt())
                        setResult(0, intent)
                        finish()
                    }, 1000)
                }
                Text(text = "Card Validated Successfully")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A,
                Bundle()
            )
        }
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableReaderMode(this)
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, "NFC must be enabled", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    override fun onTagDiscovered(tag: Tag?) {
        when(validationStateFlow.value) {
            CardValidationState.WaitingForVBConnect -> {
                val nfcData = MifareUltralight.get(tag)
                nfcData.connect()
                nfcData.use {
                    val vbData = VBNfcData(nfcData)
                    vbData.writeCardCheck(nfcData, cardIdToValidate)
                }
                validationStateFlow.value = CardValidationState.ValidateCardOnVB
            }
            CardValidationState.ValidateCardOnVB -> {
                val nfcData = MifareUltralight.get(tag)
                nfcData.connect()
                nfcData.use {
                    val vbData = VBNfcData(nfcData)
                    if(vbData.wasCardIdValidated(cardIdToValidate)) {
                        validationStateFlow.value = CardValidationState.Success
                    }
                }
            }
            else -> {
                Timber.w("Incorrect state")
            }
        }
    }
}