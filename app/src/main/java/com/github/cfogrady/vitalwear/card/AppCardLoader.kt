package com.github.cfogrady.vitalwear.card

import android.content.Context
import com.github.cfogrady.vitalwear.common.card.CardLoader
import com.github.cfogrady.vitalwear.settings.CardSettingsDao
import com.github.cfogrady.vitalwear.settings.CardSettingsEntity
import java.io.InputStream

class AppCardLoader(private val cardLoader: CardLoader, private val cardSettingsDao: CardSettingsDao) {
    fun importCard(context: Context, cardName: String, cardStream: InputStream, uniqueSprites: Boolean) {
        cardLoader.importCardImage(context, cardName, cardStream, uniqueSprites)
        cardSettingsDao.upsert(CardSettingsEntity.default(cardName))
    }
}