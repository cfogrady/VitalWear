package com.github.cfogrady.vitalwear.receive

import android.app.Activity
import android.content.Context
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.vitalwear.protos.Character
import com.github.cfogrady.vitalwear.protos.TRANSFER_CHARACTER_SERVICE_ID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

class CharacterReceiver(context: Context) {

    companion object {
        fun getMissingDependencies(activity: Activity): List<String> {
            return NearbyP2PConnection.getMissingPermissions(activity)
        }
    }

    private val nearbyP2PConnection = NearbyP2PConnection(context, TRANSFER_CHARACTER_SERVICE_ID)

    fun searchForSenders(): Flow<String> {
        nearbyP2PConnection.search()
        return nearbyP2PConnection.discoveredDevices
    }

    fun cancel() {
        nearbyP2PConnection.close()
    }

    fun connect(senderName: String): Flow<Character> {
        val characterFlow = MutableSharedFlow<Character>()
        nearbyP2PConnection.onReceive = { payload ->
            val character = Character.parseFrom(payload.asBytes())
            CoroutineScope(Dispatchers.IO).launch {
                characterFlow.emit(character)
                cancel()
            }
        }
        nearbyP2PConnection.connect(senderName)
        return characterFlow
    }
}