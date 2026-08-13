package com.dnoel.checklistv1

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChecklistViewModel(
    private val dao: ChecklistDao,
    private val listId: Int
) : ViewModel() {

    val items: Flow<List<ChecklistItem>> = dao.getForList(listId)

    fun addItem(text: String) {
        viewModelScope.launch {
            val nextPosition = dao.getMaxPosition(listId) + 1
            dao.insert(ChecklistItem(listId = listId, text = text, position = nextPosition))
        }
    }

    fun setChecked(item: ChecklistItem, checked: Boolean) {
        viewModelScope.launch {
            dao.update(item.copy(isChecked = checked))
        }
    }

    fun reorder(newOrder: List<ChecklistItem>) {
        viewModelScope.launch {
            dao.updateAll(newOrder.mapIndexed { index, item -> item.copy(position = index) })
        }
    }

    companion object {
        /** Builds a ChecklistViewModel for one list, backed by the real Room database. */
        fun factory(context: Context, listId: Int) = viewModelFactory {
            initializer {
                ChecklistViewModel(AppDatabase.getInstance(context).checklistDao(), listId)
            }
        }
    }
}
