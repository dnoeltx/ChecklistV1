package com.dnoel.checklistv1

import app.cash.turbine.test
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class ListsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun viewModel(
        listDao: FakeListDao = FakeListDao(),
        checklistDao: FakeChecklistDao = FakeChecklistDao()
    ) = ListsViewModel(listDao, checklistDao)

    @Test
    fun `lists pairs each list with its remaining unchecked count`() = runTest {
        val listDao = FakeListDao(
            listOf(
                TodoList(id = 1, name = "Groceries", position = 0),
                TodoList(id = 2, name = "Household", position = 1)
            )
        )
        val checklistDao = FakeChecklistDao(
            listOf(
                ChecklistItem(id = 1, listId = 1, text = "Milk"),
                ChecklistItem(id = 2, listId = 1, text = "Eggs"),
                ChecklistItem(id = 3, listId = 1, text = "Coffee", isChecked = true),
                ChecklistItem(id = 4, listId = 2, text = "Patch drywall")
            )
        )

        val result = viewModel(listDao, checklistDao).lists.first()

        assertEquals(listOf("Groceries", "Household"), result.map { it.list.name })
        // "Coffee" is checked off, so Groceries counts 2 remaining, not 3.
        assertEquals(listOf(2, 1), result.map { it.remainingCount })
    }

    @Test
    fun `a list with no items reports a count of zero rather than dropping out`() = runTest {
        val listDao = FakeListDao(listOf(TodoList(id = 7, name = "Camping", position = 0)))

        val result = viewModel(listDao).lists.first()

        assertEquals(1, result.size)
        assertEquals(0, result.single().remainingCount)
    }

    @Test
    fun `addList appends to the end by taking the next position`() = runTest {
        val listDao = FakeListDao(
            listOf(
                TodoList(id = 1, name = "Groceries", position = 0),
                TodoList(id = 2, name = "Household", position = 1)
            )
        )

        viewModel(listDao).addList("Camping")

        assertEquals(listOf("Groceries", "Household", "Camping"), listDao.current.map { it.name })
        assertEquals(listOf(0, 1, 2), listDao.current.map { it.position })
    }

    @Test
    fun `addList on an empty database starts at position zero`() = runTest {
        val listDao = FakeListDao()

        viewModel(listDao).addList("Groceries")

        assertEquals(0, listDao.current.single().position)
    }

    @Test
    fun `deleteList removes only the targeted list`() = runTest {
        val household = TodoList(id = 2, name = "Household", position = 1)
        val listDao = FakeListDao(
            listOf(
                TodoList(id = 1, name = "Groceries", position = 0),
                household,
                TodoList(id = 3, name = "Camping", position = 2)
            )
        )

        viewModel(listDao).deleteList(household)

        assertEquals(listOf("Groceries", "Camping"), listDao.current.map { it.name })
    }

    @Test
    fun `reorder renumbers positions from zero in the given order`() = runTest {
        val groceries = TodoList(id = 1, name = "Groceries", position = 0)
        val household = TodoList(id = 2, name = "Household", position = 1)
        val camping = TodoList(id = 3, name = "Camping", position = 2)
        val listDao = FakeListDao(listOf(groceries, household, camping))

        viewModel(listDao).reorder(listOf(camping, groceries, household))

        assertEquals(listOf("Camping", "Groceries", "Household"), listDao.current.map { it.name })
        assertEquals(listOf(0, 1, 2), listDao.current.map { it.position })
    }

    @Test
    fun `lists re-emits when a list is added`() = runTest {
        val listDao = FakeListDao()
        val viewModel = viewModel(listDao)

        viewModel.lists.test {
            assertEquals(emptyList<ListWithCount>(), awaitItem())

            viewModel.addList("Groceries")

            assertEquals(listOf("Groceries"), awaitItem().map { it.list.name })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
