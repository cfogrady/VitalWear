package com.github.cfogrady.vitalwear.logs

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vitalwear.common.communication.ChannelTypes
import com.google.android.gms.wearable.CapabilityClient
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.ChannelClient.ChannelCallback
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.File

class LogService {

    companion object {
        const val TAG = "LogService"
    }

    var initializedActivity: Activity? = null
    fun sendLogFile(context: Context, file: File) {
        initializedActivity?.let {
            sendLogFile(context, file, it)
            initializedActivity?.finish()
        }
        initializedActivity = null
    }

    @SuppressLint("LogNotTimber")
    fun sendLogFile(context: Context, file: File, activity: Activity) {
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
        }
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("vitalwear-developers@googlegroups.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "VitalWear Log")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Please take a look at these logs to help diagnose the below issue:\n")
        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        emailIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        // we have to set the clipData since we use SENDTO instead of SEND
        emailIntent.clipData = ClipData.newRawUri("", fileUri)
        emailIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        Log.i(TAG, "Starting activity to send email")
        activity.startActivity(Intent.createChooser(emailIntent, "Select Email Client"))
    }

    fun receiveFile(context: Context, channel: ChannelClient.Channel) {
        val channelClient = Wearable.getChannelClient(context)
        val logs = File(context.filesDir, "logs")
        val file = File(logs, "watch_log.txt")
        // addOnSuccessListener to receiveFile task sends file too early.
        channelClient.registerChannelCallback(channel, object: ChannelCallback() {
            override fun onInputClosed(channel: ChannelClient.Channel, closeReason: Int, appErrorCode: Int) {
                super.onInputClosed(channel, closeReason, appErrorCode)
                if(closeReason != ChannelCallback.CLOSE_REASON_NORMAL) {
                    Timber.w("Failed to received file. Close Reason: ${closeReasonString(closeReason)}")
                } else {
                    Timber.i("Successfully downloaded log file. Input closed.")
                    sendLogFile(context, file)
                }
                channelClient.close(channel)
            }
        })
        channelClient.receiveFile(channel, Uri.fromFile(file), false).apply {
            addOnFailureListener {
                Toast.makeText(context, "Error receiving log file", Toast.LENGTH_SHORT).show()
                initializedActivity?.finish()
                initializedActivity = null
            }
        }
    }

    private fun closeReasonString(closeReason: Int): String {
        return when(closeReason) {
            ChannelCallback.CLOSE_REASON_NORMAL -> "Normal"
            ChannelCallback.CLOSE_REASON_LOCAL_CLOSE -> "Closed Locally"
            ChannelCallback.CLOSE_REASON_REMOTE_CLOSE -> "Closed Remotely"
            ChannelCallback.CLOSE_REASON_DISCONNECTED -> "Disconnected"
            ChannelCallback.CLOSE_REASON_CONNECTION_TIMEOUT -> "Timeout"
            else -> "Unknown"
        }
    }

    fun fetchWatchLogs(callingActivity: Activity) {
        val messageClient = Wearable.getMessageClient(callingActivity)
        val capabilityInfoTask = Wearable.getCapabilityClient(callingActivity).getCapability(ChannelTypes.SEND_LOGS_REQUEST, CapabilityClient.FILTER_REACHABLE)
        CoroutineScope(Dispatchers.IO).launch {
            val capabilityInfo = capabilityInfoTask.await()
            for (node in capabilityInfo.nodes) {
                messageClient.sendMessage(node.id, ChannelTypes.SEND_LOGS_REQUEST, null).apply {
                    addOnFailureListener{
                        Toast.makeText(callingActivity, "Failed to send log request to watch", Toast.LENGTH_SHORT).show()
                        callingActivity.finish()
                    }
                    addOnSuccessListener {
                        initializedActivity = callingActivity
                    }
                }
            }
        }
    }
}