package com.dnoel.checklistv1

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ChecklistViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `items only includes rows belonging to this list`() = runTest {
        val dao = FakeChecklistDao(
            listOf(
                ChecklistItem(id = 1, listId = 1, text = "Milk", position = 0),
                ChecklistItem(id = 2, listId = 2, text = "Tent", position = 0),
                ChecklistItem(id = 3, listId = 1, text = "Eggs", position = 1)
            )
        )

        val result = ChecklistViewModel(dao, listId = 1).items.first()

        assertEquals(listOf("Milk", "Eggs"), result.map { it.text })
    }

    @Test
    fun `items are ordered by position, not insertion order`() = runTest {
        val dao = FakeChecklistDao(
            listOf(
                ChecklistItem(id = 1, listId = 1, text = "Headlamp", position = 2),
                ChecklistItem(id = 2, listId = 1, text = "Tent", position = 0),
                ChecklistItem(id = 3, listId = 1, text = "Sleeping bag", position = 1)
            )
        )

        val result = ChecklistViewModel(dao, listId = 1).items.first()

        assertEquals(listOf("Tent", "Sleeping bag", "Headlamp"), result.map { it.text })
    }

    @Test
    fun `addItem appends after the existing items in this list`() = runTest {
        val dao = FakeChecklistDao(
            listOf(
                ChecklistItem(id = 1, listId = 1, text = "Tent", position = 0),
                ChecklistItem(id = 2, listId = 1, text = "Headlamp", position = 1)
            )
        )

        ChecklistViewModel(dao, listId = 1).addItem("Sleeping bag")

        assertEquals(listOf("Tent", "Headlamp", "Sleeping bag"), dao.current.map { it.text })
        assertEquals(listOf(0, 1, 2), dao.current.map { it.position })
    }

    @Test
    fun `addItem numbers positions per list, not globally`() = runTest {
        val dao = FakeChecklistDao(
            listOf(
                ChecklistItem(id = 1, listId = 2, text = "Milk", position = 0),
                ChecklistItem(id = 2, listId = 2, text = "Eggs", position = 1)
            )
        )

        ChecklistViewModel(dao, listId = 1).addItem("Tent")

        // List 1 was empty, so its first item starts at 0 despite list 2 reaching 1.
        assertEquals(0, dao.current.single { it.listId == 1 }.position)
    }

    @Test
    fun `setChecked marks the item without touching its siblings`() = runTest {
        val milk = ChecklistItem(id = 1, listId = 1, text = "Milk", position = 0)
        val dao = FakeChecklistDao(
            listOf(milk, ChecklistItem(id = 2, listId = 1, text = "Eggs", position = 1))
        )

        ChecklistViewModel(dao, listId = 1).setChecked(milk, true)

        assertTrue(dao.current.single { it.id == 1 }.isChecked)
        assertEquals(false, dao.current.single { it.id == 2 }.isChecked)
    }

    @Test
    fun `checked items disappear from the remaining count`() = runTest {
        val milk = ChecklistItem(id = 1, listId = 1, text = "Milk", position = 0)
        val checklistDao = FakeChecklistDao(
            listOf(milk, ChecklistItem(id = 2, listId = 1, text = "Eggs", position = 1))
        )
        val listDao = FakeListDao(listOf(TodoList(id = 1, name = "Groceries", position = 0)))

        ChecklistViewModel(checklistDao, listId = 1).setChecked(milk, true)

        val counts = ListsViewModel(listDao, checklistDao).lists.first()
        assertEquals(1, counts.single().remainingCount)
    }

    @Test
    fun `sortByDueDate rewrites position into due-date order`() = runTest {
        val dao = FakeChecklistDao(
            listOf(
                ChecklistItem(id = 1, listId = 1, text = "Later", position = 0, dueDate = "2026-09-01"),
                ChecklistItem(id = 2, listId = 1, text = "Soon", position = 1, dueDate = "2026-08-18"),
                ChecklistItem(id = 3, listId = 1, text = "Middle", position = 2, dueDate = "2026-08-25")
            )
        )

        ChecklistViewModel(dao, listId = 1).sortByDueDate()

        // Sorting is destructive by design: position is renumbered, so the
        // previous manual order is gone rather than merely hidden.
        assertEquals(listOf("Soon", "Middle", "Later"), dao.current.map { it.text })
        assertEquals(listOf(0, 1, 2), dao.current.map { it.position })
    }

    @Test
    fun `setDueDate changes only the targeted item`() = runTest {
        val milk = ChecklistItem(id = 1, listId = 1, text = "Milk", dueDate = "2026-08-18")
        val dao = FakeChecklistDao(
            listOf(milk, ChecklistItem(id = 2, listId = 1, text = "Eggs", dueDate = "2026-08-19"))
        )

        ChecklistViewModel(dao, listId = 1).setDueDate(milk, "2026-12-25")

        assertEquals("2026-12-25", dao.current.single { it.id == 1 }.dueDate)
        assertEquals("2026-08-19", dao.current.single { it.id == 2 }.dueDate)
    }

    @Test
    fun `reorder renumbers positions from zero in the given order`() = runTest {
        val tent = ChecklistItem(id = 1, listId = 1, text = "Tent", position = 0)
        val bag = ChecklistItem(id = 2, listId = 1, text = "Sleeping bag", position = 1)
        val lamp = ChecklistItem(id = 3, listId = 1, text = "Headlamp", position = 2)
        val dao = FakeChecklistDao(listOf(tent, bag, lamp))

        ChecklistViewModel(dao, listId = 1).reorder(listOf(lamp, tent, bag))

        assertEquals(listOf("Headlamp", "Tent", "Sleeping bag"), dao.current.map { it.text })
        assertEquals(listOf(0, 1, 2), dao.current.map { it.position })
    }
}
