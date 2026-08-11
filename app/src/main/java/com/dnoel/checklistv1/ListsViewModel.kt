package com.dnoel.checklistv1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ListWithCount(val list: TodoList, val remainingCount: Int)

class ListsViewModel(application: Application) : AndroidViewModel(application) {
    private val listDao = AppDatabase.getInstance(application).listDao()
    private val checklistDao = AppDatabase.getInstance(application).checklistDao()

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
}
