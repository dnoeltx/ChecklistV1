package com.dnoel.checklistv1

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

data class ListItemCount(val listId: Int, val count: Int)

@Dao
interface ChecklistDao {
    @Query("SELECT * FROM checklist_items WHERE listId = :listId ORDER BY position")
    fun getForList(listId: Int): Flow<List<ChecklistItem>>

    @Query("SELECT listId, COUNT(*) as count FROM checklist_items WHERE isChecked = 0 GROUP BY listId")
    fun getRemainingCounts(): Flow<List<ListItemCount>>

    @Query("SELECT COALESCE(MAX(position), -1) FROM checklist_items WHERE listId = :listId")
    suspend fun getMaxPosition(listId: Int): Int

    @Insert
    suspend fun insert(item: ChecklistItem)

    @Update
    suspend fun update(item: ChecklistItem)

    @Update
    suspend fun updateAll(items: List<ChecklistItem>)
}
