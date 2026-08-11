package com.dnoel.checklistv1

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ChecklistViewModel(application: Application, private val listId: Int) : AndroidViewModel(application) {
    private val dao = AppDatabase.getInstance(application).checklistDao()

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
}
