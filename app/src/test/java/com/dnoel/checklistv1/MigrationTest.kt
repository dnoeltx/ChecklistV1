package com.dnoel.checklistv1

import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

/**
 * Proves the v3 -> v4 migration preserves real data.
 *
 * [MigrationTestHelper] builds a database from the committed 3.json schema —
 * literally the shape currently sitting on a user's phone — inserts rows, runs
 * [MIGRATION_3_4], then validates the result against 4.json. That validation is
 * what catches a migration whose DDL has drifted from what Room expects: it
 * fails here with an expected-vs-found schema dump instead of crashing later on
 * someone's device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class MigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()

    private val dbFile: File =
        File(instrumentation.targetContext.cacheDir, "migration-test.db").also { it.delete() }

    @get:Rule
    val helper = MigrationTestHelper(
        instrumentation,
        dbFile,
        AndroidSQLiteDriver(),
        AppDatabase::class
    )

    @Test
    fun `migrating 3 to 4 keeps existing rows and backfills due dates`() {
        helper.createDatabase(version = 3).use { db ->
            db.execSQL("INSERT INTO lists (id, name, position) VALUES (1, 'Groceries', 0)")
            db.execSQL("INSERT INTO lists (id, name, position) VALUES (2, 'Household', 1)")
            db.execSQL(
                "INSERT INTO checklist_items (id, listId, text, isChecked, position) " +
                    "VALUES (1, 1, 'Milk', 0, 0)"
            )
            db.execSQL(
                "INSERT INTO checklist_items (id, listId, text, isChecked, position) " +
                    "VALUES (2, 1, 'Coffee', 1, 1)"
            )
        }

        val db = helper.runMigrationsAndValidate(version = 4, migrations = listOf(MIGRATION_3_4))

        db.prepare("SELECT name, position, dueDatesEnabled FROM lists ORDER BY id").use { s ->
            assertTrue(s.step())
            assertEquals("Groceries", s.getText(0))
            assertEquals(0, s.getLong(1).toInt())
            assertEquals("due dates should default to off", 0, s.getLong(2).toInt())
            assertTrue(s.step())
            assertEquals("Household", s.getText(0))
        }

        db.prepare("SELECT text, isChecked, position, dueDate FROM checklist_items ORDER BY id")
            .use { s ->
                assertTrue(s.step())
                assertEquals("Milk", s.getText(0))
                assertEquals(0, s.getLong(1).toInt())
                assertEquals(0, s.getLong(2).toInt())
                // Backfilled to today, NOT left on the 1970 sentinel.
                assertEquals(todayIso(), s.getText(3))

                assertTrue(s.step())
                assertEquals("Coffee", s.getText(0))
                assertEquals("checked state must survive", 1, s.getLong(1).toInt())
                assertEquals(todayIso(), s.getText(3))
            }
        db.close()
    }
}

/** Small helper so connections can be used in a try-with-resources style. */
private inline fun <R> SQLiteConnection.use(block: (SQLiteConnection) -> R): R =
    try {
        block(this)
    } finally {
        close()
    }
