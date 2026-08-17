package com.dnoel.checklistv1

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * In-memory stand-ins for the Room-generated DAOs.
 *
 * They implement the same interfaces the ViewModels depend on, so the ViewModels
 * cannot tell the difference — but there is no database, no Android runtime, and
 * no I/O, so tests run in milliseconds on a plain JVM.
 *
 * State is held in a [MutableStateFlow] so that reads behave like Room's: a write
 * causes the observing Flow to re-emit.
 */
class FakeListDao(initial: List<TodoList> = emptyList()) : ListDao {

    private val rows = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    /** Current contents, for assertions. */
    val current: List<TodoList> get() = rows.value.sortedBy { it.position }

    override fun getAll(): Flow<List<TodoList>> = rows.map { list -> list.sortedBy { it.position } }

    override suspend fun getMaxPosition(): Int = rows.value.maxOfOrNull { it.position } ?: -1

    override suspend fun insert(list: TodoList): Long {
        val stored = list.copy(id = nextId++)
        rows.value = rows.value + stored
        return stored.id.toLong()
    }

    override suspend fun updateAll(lists: List<TodoList>) {
        val updates = lists.associateBy { it.id }
        rows.value = rows.value.map { existing -> updates[existing.id] ?: existing }
    }

    override suspend fun setDueDatesEnabled(listId: Int, enabled: Boolean) {
        rows.value = rows.value.map {
            if (it.id == listId) it.copy(dueDatesEnabled = enabled) else it
        }
    }

    override suspend fun delete(list: TodoList) {
        rows.value = rows.value.filterNot { it.id == list.id }
    }
}

class FakeChecklistDao(initial: List<ChecklistItem> = emptyList()) : ChecklistDao {

    private val rows = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    /** Current contents, for assertions. */
    val current: List<ChecklistItem> get() = rows.value.sortedBy { it.position }

    override fun getForList(listId: Int): Flow<List<ChecklistItem>> =
        rows.map { items -> items.filter { it.listId == listId }.sortedBy { it.position } }

    override fun getRemainingCounts(): Flow<List<ListItemCount>> =
        rows.map { items ->
            items.filterNot { it.isChecked }
                .groupingBy { it.listId }
                .eachCount()
                .map { (listId, count) -> ListItemCount(listId, count) }
        }

    override suspend fun getForListByDueDate(listId: Int): List<ChecklistItem> =
        rows.value.filter { it.listId == listId }
            .sortedWith(compareBy({ it.dueDate }, { it.position }))

    override suspend fun getMaxPosition(listId: Int): Int =
        rows.value.filter { it.listId == listId }.maxOfOrNull { it.position } ?: -1

    override suspend fun insert(item: ChecklistItem) {
        rows.value = rows.value + item.copy(id = nextId++)
    }

    override suspend fun update(item: ChecklistItem) {
        rows.value = rows.value.map { if (it.id == item.id) item else it }
    }

    override suspend fun updateAll(items: List<ChecklistItem>) {
        val updates = items.associateBy { it.id }
        rows.value = rows.value.map { existing -> updates[existing.id] ?: existing }
    }
}
