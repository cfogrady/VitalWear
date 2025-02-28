package com.github.cfogrady.vitalwear.character.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.github.cfogrady.vitalwear.VitalWearApp
import com.github.cfogrady.vitalwear.card.CardMeta
import com.github.cfogrady.vitalwear.character.CharacterManager
import com.github.cfogrady.vitalwear.common.card.CardSpritesIO
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntity
import com.github.cfogrady.vitalwear.common.card.db.CardMetaEntityDao
import kotlinx.coroutines.flow.StateFlow


/**
 * NewCardActivity is used to start a new character.
 */
class NewCharacterActivity : ComponentActivity(), NewCharacterController {

    companion object {
        const val NEW_CHARACTER_SELECTED_FLAG = "newCharacterStarted"
        const val CARD_SELECTED = "CARD_SELECTED"
        const val SLOT_SELECTED = "SLOT_SELECTED"
    }

    lateinit var characterManager : CharacterManager
    lateinit var cardSpritesIO: CardSpritesIO
    lateinit var cardMetaEntityDao: CardMetaEntityDao


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        characterManager = (application as VitalWearApp).characterManager
        cardSpritesIO = (application as VitalWearApp).cardSpriteIO
        cardMetaEntityDao = (application as VitalWearApp).cardMetaEntityDao
        setContent {
            BuildScreen(this)
        }
    }

    override fun loadCards() : List<CardMetaEntity> {
        return cardMetaEntityDao.getAll()
    }

    override fun selectCard(card: CardMetaEntity) {
        val intent = Intent()
        intent.putExtra(NEW_CHARACTER_SELECTED_FLAG, true)
        intent.putExtra(CARD_SELECTED, CardMeta.fromCardMetaEntity(card))
        setResult(0, intent)
        finish()
    }

    override val cardsImported: StateFlow<Int>
        get() = (application as VitalWearApp).cardReceiver.cardsImported
}