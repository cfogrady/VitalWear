package com.github.cfogrady.vitalwear.companion.firmware

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vb.dim.card.DimWriter
import com.github.cfogrady.vitalwear.Loading
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.github.cfogrady.vitalwear.common.composable.util.KeepScreenOn
import com.github.cfogrady.vitalwear.companion.card.ImportCardActivity
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.channels.ByteChannel
import java.nio.charset.Charset

class FirmwareImportActivity : ComponentActivity() {

    companion object {
        const val TAG = "FirmwareImportActivity"
    }

    enum class FirmwareImportState {
        PickFirmware,
        LoadFirmware,
    }

    var importState = MutableStateFlow(FirmwareImportState.PickFirmware)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firmwareImportActivity = buildFirmwareFilePickLauncher()
        setContent{
            BuildScreen(firmwareImportActivity)
        }
    }

    @Composable
    fun BuildScreen(firmwareImportActivity: ActivityResultLauncher<Array<String>>) {
        KeepScreenOn()
        val state by importState.collectAsState()
        if(state == FirmwareImportState.PickFirmware) {
            LaunchedEffect(true ) {
                firmwareImportActivity.launch(arrayOf("*/*"))
            }
        }
        Loading(loadingText = "Importing Firmware...") {}
    }

    private fun buildFirmwareFilePickLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            importState.value = FirmwareImportState.LoadFirmware
            if(it == null) {
                finish()
            } else {
                Log.i(TAG, "Path: ${it.path}")
                importFirmware(it)
            }
        }
    }

    private fun importFirmware(uri: Uri) {
        val channelClient = Wearable.getChannelClient(this)
        val nodeListTask = Wearable.getNodeClient(this).connectedNodes
        lifecycleScope.launch {
            val nodes = nodeListTask.await()
            lateinit var firmware: ByteArray
            contentResolver.openInputStream(uri).use {
                firmware = it!!.readBytes()
            }
            for (node in nodes) {
                val channel = channelClient.openChannel(node.id, ChannelTypes.FIRMWARE_DATA).await()
                channelClient.registerChannelCallback(channel, object: ChannelClient.ChannelCallback() {
                    override fun onOutputClosed(p0: ChannelClient.Channel, p1: Int, p2: Int) {
                        super.onOutputClosed(p0, p1, p2)
                        Log.i(TAG, "Closing channel to watch")
                        channelClient.close(p0)
                        finish()
                    }
                })
                // We don't use send file because we can't make use of the uri received from the file picker with sendFile. We need a full file path, to which we don't have access.
                channelClient.getOutputStream(channel).await().use {
                    Log.i(TAG, "Writing to firmware to watch")
                    it.write(firmware)
                    Log.i(TAG, "Done writing to firmware to watch")
                }
            }
        }
    }
}