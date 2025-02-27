package com.github.cfogrady.vitalwear

import androidx.datastore.core.DataStore
import kotlinx.coroutines.flow.Flow

class SaveDataRepository(
    private val saveDataStore: DataStore<SaveData>
) {
    val saveDataFlow: Flow<SaveData> = saveDataStore.data

    suspend fun togglePermissionsAlreadyRequested() {
        saveDataStore.updateData { currentSaveData->
            currentSaveData.toBuilder().setPermissionsAlreadyRequested(true).build()
        }
    }
}