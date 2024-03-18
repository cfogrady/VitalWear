package com.github.cfogrady.vitalwear.character.data

import android.content.Context
import com.github.cfogrady.vitalwear.common.card.CardCharacterImageService
import com.github.cfogrady.vitalwear.common.card.CharacterSpritesIO
import timber.log.Timber
import java.util.HashMap
import java.util.HashSet

class PreviewCharacterManager(private val characterDao: CharacterDao, private val cardCharacterImageService: CardCharacterImageService) {
    fun previewCharacters(applicationContext: Context) : List<CharacterPreview> {
        Timber.i("Fetching preview characters")
        val characters = characterDao.getCharactersOrderByRecent()
        Timber.i("Retrieved. Building slots needed map.")
        val slotsNeededByCard = HashMap<String, MutableSet<Int>>()
        for(character in characters) {
            val slotSet = slotsNeededByCard.getOrPut(character.cardFile) { HashSet<Int>() }
            slotSet.add(character.slotId)
        }
        Timber.i("Built. Reading Card Data.")
        val bitmapsByCardNameAndSlotId = cardCharacterImageService.loadBitmapsForSlots(applicationContext, slotsNeededByCard, CharacterSpritesIO.IDLE1)
        Timber.i("Read. Creating CharacterPreview objects")
        val previewCharacters = ArrayList<CharacterPreview>()
        for(character in characters) {
            val idleBitmap = bitmapsByCardNameAndSlotId.get(character.cardFile)!!.get(character.slotId)
            previewCharacters.add(CharacterPreview(character.cardFile, character.slotId, character.id, character.state, idleBitmap))
        }
        Timber.i("Ready")
        return previewCharacters
    }
}