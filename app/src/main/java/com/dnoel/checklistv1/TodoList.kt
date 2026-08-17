package com.dnoel.checklistv1

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lists")
data class TodoList(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val position: Int = 0,
    // Off by default, so existing lists are visually unchanged after upgrading.
    @ColumnInfo(defaultValue = "0")
    val dueDatesEnabled: Boolean = false
)
