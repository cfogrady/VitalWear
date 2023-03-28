package com.github.cfogrady.vitalwear.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.cfogrady.vitalwear.character.data.CharacterDao
import com.github.cfogrady.vitalwear.character.data.CharacterEntity

@Database(entities = [CharacterEntity::class], version = 1)
@TypeConverters(LocalDateTimeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun characterDao(): CharacterDao
}