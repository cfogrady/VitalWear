package com.github.cfogrady.vitalwear.common.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

class CreateAndPopulateMaxAdventureCompletionCardMeta : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("alter table card_meta add column maxAdventureCompletion integer")
        val cursor = db.query("select cardName, max(adventureId) from adventure where cleared = 1 group by cardName")
        cursor.use {
            while(it.moveToNext()) {
                val cardName = it.getString(0)
                val maxAdventureCompletion = it.getInt(1)
                db.execSQL("update card_meta set maxAdventureCompletion = $maxAdventureCompletion where cardName = '$cardName'")
            }
        }
    }
}