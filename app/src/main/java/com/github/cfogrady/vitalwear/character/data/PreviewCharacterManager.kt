package com.github.cfogrady.vitalwear.character.data

import android.content.Context
import android.util.Log
import com.github.cfogrady.vitalwear.card.CharacterSpritesIO
import com.github.cfogrady.vitalwear.card.CardLoader
import java.util.HashMap
import java.util.HashSet

class PreviewCharacterManager(val characterDao: CharacterDao, val cardLoader: CardLoader) {
    fun previewCharacters(applicationContext: Context) : List<CharacterPreview> {
        Log.i(com.github.cfogrady.vitalwear.character.TAG, "Fetching preview characters")
        val characters = characterDao.getCharactersOrderByRecent()
        Log.i(com.github.cfogrady.vitalwear.character.TAG, "Retrieved. Building slots needed map.")
        val slotsNeededByCard = HashMap<String, MutableSet<Int>>()
        for(character in characters) {
            val slotSet = slotsNeededByCard.getOrPut(character.cardFile) { HashSet<Int>() }
            slotSet.add(character.slotId)
        }
        Log.i(com.github.cfogrady.vitalwear.character.TAG, "Built. Reading Card Data.")
        val bitmapsByCardNameAndSlotId = cardLoader.loadBitmapsForSlots(applicationContext, slotsNeededByCard, CharacterSpritesIO.IDLE1)
        Log.i(com.github.cfogrady.vitalwear.character.TAG, "Read. Creating CharacterPreview objects")
        val previewCharacters = ArrayList<CharacterPreview>()
        for(character in characters) {
            val idleBitmap = bitmapsByCardNameAndSlotId.get(character.cardFile)!!.get(character.slotId)
            previewCharacters.add(CharacterPreview(character.cardFile, character.slotId, character.id, idleBitmap))
        }
        Log.i(com.github.cfogrady.vitalwear.character.TAG, "Ready")
        return previewCharacters
    }
}