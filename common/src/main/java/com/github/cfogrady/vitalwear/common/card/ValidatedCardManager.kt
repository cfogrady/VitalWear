package com.github.cfogrady.vitalwear.common.card

import android.util.Log
import com.github.cfogrady.vitalwear.common.card.db.ValidatedCardEntity
import com.github.cfogrady.vitalwear.common.card.db.ValidatedCardEntityDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.IllegalStateException

class ValidatedCardManager(private val validatedCardEntityDao: ValidatedCardEntityDao) {

    companion object {
        const val TAG = "ValidatedCardManager"
    }

    lateinit var validatedIds: Set<Int>

    init {
        GlobalScope.launch(Dispatchers.IO) {
            validatedIds = HashSet(validatedCardEntityDao.getIds())
        }
    }

    fun addValidatedCard(cardId: Int) {
        if (!isInitialized()) {
            throw IllegalStateException("Not yet initialized")
        }
        validatedCardEntityDao.insert(ValidatedCardEntity(cardId))
        validatedIds = validatedIds.plus(cardId)
    }

    fun isValidatedCard(cardId: Int): Boolean {
        Log.i(TAG, "Checking if $cardId is contained in $validatedIds")
        if (!isInitialized()) {
            throw IllegalStateException("Not yet initialized")
        }
        return true
        // return validatedIds.contains(cardId)
    }

    fun isInitialized(): Boolean {
        return this::validatedIds.isInitialized
    }
}