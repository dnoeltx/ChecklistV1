package com.dnoel.checklistv1

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ListWithCount(val list: TodoList, val remainingCount: Int)

class ListsViewModel(
    private val listDao: ListDao,
    private val checklistDao: ChecklistDao
) : ViewModel() {

    val lists: Flow<List<ListWithCount>> = combine(
        listDao.getAll(),
        checklistDao.getRemainingCounts()
    ) { lists, counts ->
        val countsByListId = counts.associate { it.listId to it.count }
        lists.map { list -> ListWithCount(list, countsByListId[list.id] ?: 0) }
    }

    fun addList(name: String) {
        viewModelScope.launch {
            val nextPosition = listDao.getMaxPosition() + 1
            listDao.insert(TodoList(name = name, position = nextPosition))
        }
    }

    fun deleteList(list: TodoList) {
        viewModelScope.launch {
            listDao.delete(list)
        }
    }

    fun reorder(newOrder: List<TodoList>) {
        viewModelScope.launch {
            listDao.updateAll(newOrder.mapIndexed { index, list -> list.copy(position = index) })
        }
    }

    companion object {
        /** Builds a ListsViewModel backed by the real Room database. */
        fun factory(context: Context) = viewModelFactory {
            initializer {
                val db = AppDatabase.getInstance(context)
                ListsViewModel(db.listDao(), db.checklistDao())
            }
        }
    }
}
