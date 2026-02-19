package com.ezhart.todotxtandroid

import com.ezhart.todotxtandroid.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class TaskUnitTests {

    @Test
    fun task_valid_priority() {
        val taskData = "(A) This is a test task"

        val task1 = Task(taskData)
        assertEquals('A', task1.priority)

        // Validate the helper method
        assertEquals('A', Task.parsePriority(taskData))
    }

    @Test
    fun task_priority_must_be_uppercase() {
        val task2 = Task("(a) This is a test task")
        assertEquals(null, task2.priority)
    }

    @Test
    fun task_priority_must_be_first() {
        val task2 = Task(" (A) test")
        assertEquals(null, task2.priority)
    }

    @Test
    fun task_priority_must_be_letter() {
        val task1 = Task("(2) This is a test task")
        assertEquals(null, task1.priority)
    }

    @Test
    fun task_priority_not_included_in_body() {
        val task1 = Task("(A) This is a test task")
        val task2 = Task("(a) This is a test task")

        assertEquals("This is a test task", task1.body)
        assertEquals("(a) This is a test task", task2.body)
    }

    @Test
    fun task_completed_date() {
        val task = Task("x 2025-01-03 some other info")

        assertEquals(true, task.completed)
        assertEquals(LocalDate.of(2025, 1, 3), task.completedDate)
    }

    // TODO completed mark and date not included in body

    @Test
    fun task_completed_must_be_lowercase() {
        val task = Task("X 2025-01-03 some other info")

        assertEquals(false, task.completed)
        assertEquals(null, task.completedDate)
    }

    @Test
    fun task_completed_date_must_be_valid() {
        val task = Task("x 2025-02-31 not a real date")

        assertEquals(true, task.completed)
        assertEquals(null, task.completedDate)
    }

    @Test
    fun task_complete_with_created_date() {
        val task = Task("x 2025-02-01 2025-01-01 task data")
        assertEquals(LocalDate.of(2025, 1, 1), task.createdDate)
    }

    @Test
    fun task_pending_with_created_date() {
        val task = Task("2025-01-01 task data")
        assertEquals(LocalDate.of(2025, 1, 1), task.createdDate)
    }

    @Test
    fun task_created_date_not_included_in_body() {
        val task1 = Task("(A) 2025-09-08 This is a test task")
        assertEquals("This is a test task", task1.body)

        val task2 = Task("2025-09-08 This is a test task")
        assertEquals("This is a test task", task2.body)
    }


    @Test
    fun task_completed_info_not_included_in_body() {
        val task = Task("x 2025-09-08 This is a test task")
        assertEquals("This is a test task", task.body)
    }

    @Test
    fun task_priority_with_created_date() {
        val task = Task("(B) 2025-01-01 task data")
        assertEquals(LocalDate.of(2025, 1, 1), task.createdDate)
    }

    @Test
    fun task_created_date_must_be_valid() {
        val task = Task("2025-02-31 task data")
        assertEquals(null, task.createdDate)
    }

    // TODO projects

    // TODO contexts

    // TODO metadata

}