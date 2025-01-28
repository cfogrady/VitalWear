package com.github.cfogrady.vitalwear.transfer

import android.app.Activity
import android.content.Context
import com.github.cfogrady.nearby.connections.p2p.NearbyP2PConnection
import com.github.cfogrady.vitalwear.protos.Character
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface CharacterTransfer {

    companion object {
        fun getMissingPermissions(activity: Activity): List<String> {
            return NearbyP2PConnection.getMissingPermissions(activity)
        }

        fun getInstance(context: Context): CharacterTransfer {
            return CharacterTransferImpl(context)
        }
    }

    enum class Result {
        TRANSFERRING,
        SUCCESS,
        REJECTED,
        FAILURE,
    }


    val deviceName: String

    fun searchForOtherTransferDevices(): Flow<String>

    fun close()

    fun receiveCharacterFrom(senderName: String, receive: suspend (Character)->Boolean): StateFlow<Result>


    fun sendCharacterToDevice(senderName: String, character: Character): StateFlow<Result>
}