package com.github.cfogrady.vitalwear.card

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

class ValidatedCardManager(private val validatedCardEntityDao: ValidatedCardEntityDao) {

    companion object {
        const val TAG = "ValidatedCardManager"
    }

    private lateinit var validatedIds: Set<Int>

    init {
        CoroutineScope(Dispatchers.IO).launch {
            validatedIds = HashSet(validatedCardEntityDao.getIds())
        }
    }

    suspend fun addValidatedCard(cardId: Int) {
        if (!isInitialized()) {
            throw IllegalStateException("Not yet initialized")
        }
        withContext(Dispatchers.IO) {
            validatedCardEntityDao.insert(ValidatedCardEntity(cardId))
            validatedIds = validatedIds.plus(cardId)
        }
    }

    fun isValidatedCard(cardId: Int): Boolean {
        Log.i(TAG, "Checking if $cardId is contained in $validatedIds")
        if (!isInitialized()) {
            throw IllegalStateException("Not yet initialized")
        }
        return validatedIds.contains(cardId)
    }

    private fun isInitialized(): Boolean {
        return this::validatedIds.isInitialized
    }
}