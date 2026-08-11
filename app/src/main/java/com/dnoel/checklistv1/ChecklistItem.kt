package com.dnoel.checklistv1

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    val position: Int = 0
)
