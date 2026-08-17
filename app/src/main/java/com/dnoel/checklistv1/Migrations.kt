package com.dnoel.checklistv1

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * v3 -> v4: due dates.
 *
 * Adds `checklist_items.dueDate` and `lists.dueDatesEnabled`.
 *
 * Two things worth noting about the item column:
 *
 *  - SQLite requires a DEFAULT when adding a NOT NULL column, and forbids
 *    non-constant defaults such as CURRENT_DATE. Hence the literal sentinel,
 *    which must match [DUE_DATE_SQL_DEFAULT] exactly or Room's schema
 *    validation will reject the migrated database.
 *  - The sentinel is immediately replaced: existing items are backfilled to
 *    today via SQLite's own date() function, so no row is left showing 1970.
 *
 * Lists default to due dates *off*, so upgrading changes nothing visible until
 * the user turns the feature on for a specific list.
 */
val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE checklist_items " +
                "ADD COLUMN dueDate TEXT NOT NULL DEFAULT $DUE_DATE_SQL_DEFAULT"
        )
        db.execSQL("UPDATE checklist_items SET dueDate = date('now', 'localtime')")
        db.execSQL(
            "ALTER TABLE lists ADD COLUMN dueDatesEnabled INTEGER NOT NULL DEFAULT 0"
        )
    }
}
