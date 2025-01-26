package com.github.cfogrady.vitalwear.transfer

import android.app.Activity
import android.content.Context
import com.github.cfogrady.nearby.connections.p2p.ConnectionStatus
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.vitalwear.protos.Character
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

class CharacterTransfer(context: Context) {
    companion object {
        val READY_SIGNAL = "READY".toByteArray(StandardCharsets.UTF_8)
        val RECEIVED_SIGNAL = "RECEIVED".toByteArray(StandardCharsets.UTF_8)
        val REJECTED_SIGNAL = "REJECTED".toByteArray(StandardCharsets.UTF_8)

        const val TRANSFER_CHARACTER_SERVICE_ID = "com.github.cfogrady.vitalwear.transfer"

        fun getMissingDependencies(activity: Activity): List<String> {
            return NearbyP2PConnection.getMissingPermissions(activity)
        }
    }

    enum class Result {
        TRANSFERRING,
        SUCCESS,
        REJECTED,
        FAILURE,
    }

    private val nearbyP2PConnection = NearbyP2PConnection(context, TRANSFER_CHARACTER_SERVICE_ID)

    val deviceName = nearbyP2PConnection.pairingName

    fun searchForOtherTransferDevices(): Flow<String> {
        nearbyP2PConnection.search()
        return nearbyP2PConnection.discoveredDevices
    }

    fun close() {
        nearbyP2PConnection.close()
    }

    fun receiveCharacterFrom(senderName: String, receive: suspend (Character)->Boolean): StateFlow<Result> {
        var result = MutableStateFlow(Result.TRANSFERRING)
        nearbyP2PConnection.onReceive = { payload ->
            CoroutineScope(Dispatchers.IO).launch {
                val character = Character.parseFrom(payload.asBytes())
                if(receive(character)) {
                    nearbyP2PConnection.sendData(RECEIVED_SIGNAL)
                    result.update { Result.SUCCESS }
                } else {
                    nearbyP2PConnection.sendData(REJECTED_SIGNAL)
                    result.update { Result.REJECTED }
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
                    nearbyP2PConnection.sendData(READY_SIGNAL)
                }
            }
            if(result.value == Result.TRANSFERRING) {
                result.update { Result.FAILURE }
            }
            close()
        }
        nearbyP2PConnection.connect(senderName)
        return result
    }


    fun sendCharacterToDevice(senderName: String, character: Character): StateFlow<Result> {
        val result = MutableStateFlow(Result.TRANSFERRING)
        nearbyP2PConnection.onReceive = { payload ->
            if(payload.asBytes().contentEquals(READY_SIGNAL)) {
                nearbyP2PConnection.sendData(character.toByteArray())
            } else if (payload.asBytes().contentEquals(RECEIVED_SIGNAL)) {
                result.update { Result.SUCCESS }
                close()
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            nearbyP2PConnection.connectionStatus.transformWhile{
                emit(it)
                if(it == ConnectionStatus.REJECTED) {
                    result.update { Result.REJECTED }
                }
                it != ConnectionStatus.REJECTED && it != ConnectionStatus.DISCONNECTED && it != ConnectionStatus.IDLE
            }.collect {}
            // disconnected
            close()
            if(result.value == Result.TRANSFERRING) {
                // premature disconnect
                result.update { Result.FAILURE }
            }
        }
        nearbyP2PConnection.connect(senderName)
        return result
    }
}