package com.github.cfogrady.vitalwear.receive

import com.github.cfogrady.nearby.connections.p2p.ConnectionStatus
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.google.android.gms.nearby.connection.Payload
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CharacterReceiver(val nearbyP2PConnection: NearbyP2PConnection) {
    init {
        if(nearbyP2PConnection.connectionStatus.value != ConnectionStatus.CONNECTED) {
            throw IllegalStateException("Character receiver relies on a connection having already been established")
        }
        nearbyP2PConnection.onReceive = this::onReceive
    }

    fun onReceive(payload: Payload) {
        CoroutineScope(Dispatchers.IO).launch {

        }
    }

    suspend fun receiveCharacter()  {

    }
}