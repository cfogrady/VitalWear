package com.github.cfogrady.vitalwear.receive

import com.github.cfogrady.nearby.connections.p2p.ConnectionStatus
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.vitalwear.protos.Character
import com.google.android.gms.nearby.connection.Payload

class CharacterReceiver(val nearbyP2PConnection: NearbyP2PConnection) {
    init {
        if(nearbyP2PConnection.connectionStatus.value != ConnectionStatus.CONNECTED) {
            throw IllegalStateException("Character receiver relies on a connection having already been established")
        }
        nearbyP2PConnection.onReceive = this::onReceive
    }

    fun onReceive(payload: Payload) {
        Character.parseFrom(payload.asBytes())
    }

    suspend fun receiveCharacter()  {

    }
}