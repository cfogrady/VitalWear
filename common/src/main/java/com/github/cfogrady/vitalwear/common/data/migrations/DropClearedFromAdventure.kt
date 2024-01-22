package com.github.cfogrady.vitalwear.common.data.migrations

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec

@DeleteColumn(
    tableName = "adventure",
    columnName = "cleared"
)
class DropClearedFromAdventure : AutoMigrationSpec {
}