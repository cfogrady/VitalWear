package com.github.cfogrady.vitalwear.data

import androidx.core.database.getIntOrNull
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.github.cfogrady.vitalwear.common.data.migrations.CreateAndPopulateMaxAdventureCompletionCardMeta
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class Version2MigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName!!,
        FrameworkSQLiteOpenHelperFactory()
    )

    @Test
    @Throws(IOException::class)
    fun migrate1To2() {
        var db = helper.createDatabase(TEST_DB, 1).apply {
            // Database has schema version 1. Insert some data using SQL queries.
            // You can't use DAO classes because they expect the latest schema.
            execSQL("insert into card_meta values('halfCompletedCard', 0, 0, 'BEM', 0)")
            setupAdventures(this, "halfCompletedCard",12, 5)
            execSQL("insert into card_meta values('notStartedCard', 0, 0, 'BEM', 0)")
            setupAdventures(this, "notStartedCard",12)
            execSQL("insert into card_meta values('singleCompletedCard', 0, 0, 'BEM', 0)")
            setupAdventures(this, "singleCompletedCard",12, 0)
            execSQL("insert into card_meta values('fullyCompletedCard', 0, 0, 'BEM', 0)")
            setupAdventures(this, "fullyCompletedCard",12, 11)

            // Prepare for the next version.
            close()
        }

        // Re-open the database with version 2 and provide
        // MIGRATION_1_2 as the migration process.
        db = helper.runMigrationsAndValidate(TEST_DB, 2, true, CreateAndPopulateMaxAdventureCompletionCardMeta())

        // MigrationTestHelper automatically verifies the schema changes,
        // but you need to validate that the data was migrated properly.
        validateCard(db, "halfCompletedCard", 5)
        validateCard(db, "notStartedCard", null)
        validateCard(db, "singleCompletedCard", 0)
        validateCard(db, "fullyCompletedCard", 11)
    }
}

private fun setupAdventures(db: SupportSQLiteDatabase, cardName: String, adventures: Int, lastCompleted: Int = -1) {
    for(i in 0 until adventures) {
        db.execSQL("insert into adventure values ('$cardName', $i, 500, ${i+2}, null, null, null, null, null, 0, 0, 0, null, ${if(lastCompleted >= i) 1 else 0})")
    }
}

private fun validateCard(db: SupportSQLiteDatabase, cardName: String, lastCompletedAdventure: Int?) {
    val cursor = db.query("select maxAdventureCompletion from card_meta where cardName = '$cardName'")
    cursor.use {
        while(cursor.moveToNext()) {
            val actualMaxAdventureCompletion = cursor.getIntOrNull(0)
            Assert.assertEquals(lastCompletedAdventure, actualMaxAdventureCompletion)
        }
    }
}