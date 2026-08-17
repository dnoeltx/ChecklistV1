package com.dnoel.checklistv1

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TodoList::class, ChecklistItem::class], version = 4)
abstract class AppDatabase : RoomDatabase() {
    abstract fun checklistDao(): ChecklistDao
    abstract fun listDao(): ListDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "checklist.db"
                )
                    // No destructive fallback any more: a schema change without
                    // a matching Migration should fail loudly in development
                    // rather than silently wipe a user's data.
                    .addMigrations(MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
        }
    }
}
