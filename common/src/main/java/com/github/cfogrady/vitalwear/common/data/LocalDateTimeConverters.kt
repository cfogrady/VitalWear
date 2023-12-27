package com.github.cfogrady.vitalwear.common.data

import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.ZoneOffset

class LocalDateTimeConverters {
    @TypeConverter
    fun toLocalDateTime(value: Long?) : LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime: LocalDateTime?) : Long? {
        return dateTime?.toEpochSecond(ZoneOffset.UTC)
    }
}