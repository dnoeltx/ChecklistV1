package com.dnoel.checklistv1

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ListDao {
    @Query("SELECT * FROM lists ORDER BY position")
    fun getAll(): Flow<List<TodoList>>

    @Query("SELECT COALESCE(MAX(position), -1) FROM lists")
    suspend fun getMaxPosition(): Int

    @Insert
    suspend fun insert(list: TodoList): Long

    @Update
    suspend fun updateAll(lists: List<TodoList>)

    @Query("UPDATE lists SET dueDatesEnabled = :enabled WHERE id = :listId")
    suspend fun setDueDatesEnabled(listId: Int, enabled: Boolean)

    @Delete
    suspend fun delete(list: TodoList)
}
