package com.github.cfogrady.vitalwear.common.card

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.SparseArray
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import java.util.HashMap

class CardCharacterImageService(
    private val speciesEntityDao: SpeciesEntityDao,
    private val characterSpritesIO: CharacterSpritesIO,
) {
    companion object {
        const val TAG = "CardCharacterImageService"
    }

    fun loadBitmapsForSlots(applicationContext: Context, requestedSlotsByCardName : Map<String, Collection<Int>>, spriteFile: String) : Map<String, SparseArray<Bitmap>> {
        val resultMap = HashMap<String, SparseArray<Bitmap>>()
        for(entry in requestedSlotsByCardName.entries) {
            Log.i(TAG, "Reading stats $entry")
            val speciesStats = speciesEntityDao.getCharacterByCardAndInCharacterIds(entry.key, entry.value)
            Log.i(TAG, "Card read.")
            val cardSlots = SparseArray<Bitmap>()
            for(speciesStat in speciesStats) {
                Log.i(TAG, "Fetching sprite for slot ${speciesStat.characterId}")
                val bitmap = characterSpritesIO.loadCharacterBitmapFile(applicationContext, speciesStat.spriteDirName, spriteFile)
                Log.i(TAG, "Done.")
                cardSlots.put(speciesStat.characterId, bitmap)
            }
            resultMap[entry.key] = cardSlots
        }
        Log.i(TAG, "BitmapsForSlots Built")
        return resultMap
    }
}