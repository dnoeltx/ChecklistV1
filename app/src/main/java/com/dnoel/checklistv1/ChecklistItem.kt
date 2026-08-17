package com.dnoel.checklistv1

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * SQLite forbids a non-constant DEFAULT on ALTER TABLE ADD COLUMN — CURRENT_DATE
 * is explicitly rejected — so a NOT NULL column has to be added with a literal.
 * The migration adds the column with this sentinel and then backfills existing
 * rows to today. Room compares this declared default against the real schema,
 * so the two must match exactly.
 */
const val DUE_DATE_SQL_DEFAULT = "'1970-01-01'"

@Entity(
    tableName = "checklist_items",
    foreignKeys = [
        ForeignKey(
            entity = TodoList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("listId")]
)
data class ChecklistItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val listId: Int,
    val text: String,
    val isChecked: Boolean = false,
    val position: Int = 0,
    // The Kotlin default is today, so anything the app inserts gets a real date.
    // The SQL default above only ever applies to rows that existed before the
    // column did, and the migration immediately backfills those.
    @ColumnInfo(defaultValue = DUE_DATE_SQL_DEFAULT)
    val dueDate: String = todayIso()
)
