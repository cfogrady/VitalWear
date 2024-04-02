package com.github.cfogrady.vitalwear.common.card

import android.content.Context
import android.graphics.Bitmap
import android.util.SparseArray
import com.github.cfogrady.vitalwear.common.card.db.SpeciesEntityDao
import timber.log.Timber
import java.util.HashMap

class CardCharacterImageService(
    private val speciesEntityDao: SpeciesEntityDao,
    private val characterSpritesIO: CharacterSpritesIO,
) {
    fun loadBitmapsForSlots(applicationContext: Context, requestedSlotsByCardName : Map<String, Collection<Int>>, spriteFile: String) : Map<String, SparseArray<Bitmap>> {
        val resultMap = HashMap<String, SparseArray<Bitmap>>()
        for(entry in requestedSlotsByCardName.entries) {
            Timber.i("Reading stats $entry")
            val speciesStats = speciesEntityDao.getCharacterByCardAndInCharacterIds(entry.key, entry.value)
            Timber.i("Card read.")
            val cardSlots = SparseArray<Bitmap>()
            for(speciesStat in speciesStats) {
                Timber.i("Fetching sprite for slot ${speciesStat.characterId}")
                val bitmap = characterSpritesIO.loadCharacterBitmapFile(applicationContext, speciesStat.spriteDirName, spriteFile)
                Timber.i("Done.")
                cardSlots.put(speciesStat.characterId, bitmap)
            }
            resultMap[entry.key] = cardSlots
        }
        Timber.i("BitmapsForSlots Built")
        return resultMap
    }
}