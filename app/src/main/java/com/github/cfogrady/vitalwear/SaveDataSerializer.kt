package com.github.cfogrady.vitalwear

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream


private const val SAVE_DATA_STORE_NAME = "save_data.pb"

val Context.saveDataStore: DataStore<SaveData> by dataStore(
    fileName = SAVE_DATA_STORE_NAME,
    serializer = SettingsSerializer
)

object SettingsSerializer: Serializer<SaveData> {
    override val defaultValue = SaveData.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): SaveData {
        try {
            return SaveData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: SaveData, output: OutputStream) {
        t.writeTo(output)
    }

}