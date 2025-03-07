package com.github.cfogrady.vitalwear.transfer

import android.content.Context
import android.util.Log
import com.github.cfogrady.nearby.connections.p2p.ConnectionStatus
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.vitalwear.protos.Character
import com.github.cfogrady.vitalwear.protos.ConnectMeta
import com.github.cfogrady.vitalwear.transfer.CharacterTransfer.Result
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transformWhile
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.nio.charset.StandardCharsets

internal class CharacterTransferImpl(context: Context): CharacterTransfer {
    companion object {
        const val TAG = "CharacterTransferImpl"
        val READY_SIGNAL = "READY"
        val RECEIVED_SIGNAL = "RECEIVED".toByteArray(StandardCharsets.UTF_8)
        val REJECTED_SIGNAL = "REJECTED".toByteArray(StandardCharsets.UTF_8)

        const val TRANSFER_CHARACTER_SERVICE_ID = "com.github.cfogrady.vitalwear.transfer"

        val SUPPORTED_VERSIONS = setOf(1)

    }

    private val nearbyP2PConnection = NearbyP2PConnection(context, TRANSFER_CHARACTER_SERVICE_ID)

    override val deviceName = nearbyP2PConnection.pairingName

    override fun searchForOtherTransferDevices(): Flow<String> {
        nearbyP2PConnection.search()
        return nearbyP2PConnection.discoveredDevices
    }

    override fun close() {
        nearbyP2PConnection.close()
    }

    override fun receiveCharacterFrom(senderName: String, receive: suspend (Character)->Boolean): StateFlow<Result> {
        var result = MutableStateFlow(Result.TRANSFERRING)
        var packetsReceived = 0
        nearbyP2PConnection.onReceive = { payload ->
            CoroutineScope(Dispatchers.IO).launch {
                when(packetsReceived) {
                    0 -> {
                        val character = Character.parseFrom(payload.asBytes())
                        if(receive(character)) {
                            Log.i(TAG, "Send Received Signal")
                            nearbyP2PConnection.sendData(RECEIVED_SIGNAL)
                            result.update { Result.SUCCESS }
                        } else {
                            Log.i(TAG, "Send Rejected Signal")
                            nearbyP2PConnection.sendData(REJECTED_SIGNAL)
                            result.update { Result.REJECTED }
                        }
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            nearbyP2PConnection.connectionStatus.takeWhile {
                if(it == ConnectionStatus.REJECTED) {
                    result.update { Result.REJECTED }
                }
                it != ConnectionStatus.DISCONNECTED && it != ConnectionStatus.IDLE && it != ConnectionStatus.REJECTED
            }.collect {
                if(it == ConnectionStatus.CONNECTED) {
                    Log.i(TAG, "Send Ready Signal")
                    val connectMeta = ConnectMeta.newBuilder()
                        .setReadyIndicator(READY_SIGNAL)
                        .addAllSupportedVersions(listOf(1))
                        .build()
                    nearbyP2PConnection.sendData(connectMeta.toByteArray())
                }
            }
            if(result.value == Result.TRANSFERRING) {
                result.update {
                    Log.i(TAG, "Transfer Failed")
                    Result.FAILURE
                }
            }
            close()
        }
        nearbyP2PConnection.connect(senderName)
        return result
    }

    fun findMaxSharedVersion(otherVersions: Set<Int>): Int {
        val matchingVersions = SUPPORTED_VERSIONS.intersect(otherVersions)
        return matchingVersions.last()
    }

    override fun sendCharacterToDevice(senderName: String, character: Character): StateFlow<Result> {
        val result = MutableStateFlow(Result.TRANSFERRING)
        var packetsReceived = 0
        nearbyP2PConnection.onReceive = { payload ->
            CoroutineScope(Dispatchers.IO).launch {
                when(packetsReceived) {
                    0 -> {
                        try {
                            val connectionMeta = ConnectMeta.parseFrom(payload.asBytes())
                            if(READY_SIGNAL == connectionMeta.readyIndicator) {
                                packetsReceived++
                                // val useVersion = findMaxSharedVersion(connectionMeta.supportedVersionsList.toSet())
                                Log.i(TAG, "Received Ready Signal. Sending Character")
                                nearbyP2PConnection.sendData(character.toByteArray())
                            }
                        } catch (e: InvalidProtocolBufferException) {
                            Log.i(TAG, "Expected ConnectMeta was invalid")
                            result.update { Result.FAILURE }
                            close()
                        }
                    }
                    1 -> {
                        if (payload.asBytes().contentEquals(RECEIVED_SIGNAL)) {
                            Log.i(TAG, "Received Success Signal.")
                            result.update { Result.SUCCESS }
                            close()
                        } else {
                            Log.i(TAG, "Received Unexpected Signal.")
                            result.update { Result.FAILURE }
                            close()
                        }
                    }
                }
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            nearbyP2PConnection.connectionStatus.transformWhile{
                emit(it)
                if(it == ConnectionStatus.REJECTED) {
                    result.update { Result.REJECTED }
                }
                it != ConnectionStatus.REJECTED && it != ConnectionStatus.DISCONNECTED && it != ConnectionStatus.IDLE
            }.collect {} // blocks until previous statement evaluates as false
            // disconnected
            close()
            if(result.value == Result.TRANSFERRING) {
                // premature disconnect
                result.update {
                    Log.i(TAG, "Transfer Failed")
                    Result.FAILURE
                }
            }
        }
        nearbyP2PConnection.connect(senderName)
        return result
    }

}