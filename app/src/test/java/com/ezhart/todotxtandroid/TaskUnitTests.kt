package com.ezhart.todotxtandroid

import com.ezhart.todotxtandroid.data.NoPriority
import com.ezhart.todotxtandroid.data.Priority
import com.ezhart.todotxtandroid.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.experimental.results.PrintableResult
import java.time.LocalDate

class TaskUnitTests {

    @Test
    fun task_valid_priority() {
        val taskData = "(A) This is a test task"

        val task1 = Task(taskData)
        val expectedPriority = Priority('A')
        assertEquals(expectedPriority, task1.taskPriority)

        // Validate the helper method
        assertEquals(expectedPriority, Task.parsePriority(taskData))
    }

    @Test
    fun task_priority_must_be_uppercase() {
        val task2 = Task("(a) This is a test task")
        assertEquals(NoPriority, task2.taskPriority)
    }

    @Test
    fun task_priority_must_be_first() {
        val task2 = Task(" (A) test")
        assertEquals(NoPriority, task2.taskPriority)
    }

    @Test
    fun task_priority_must_be_letter() {
        val task1 = Task("(2) This is a test task")
        assertEquals(NoPriority, task1.taskPriority)
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

    @Test
    fun task_with_project(){
        val task = Task("2025-02-31 buy tofu +shopping")
        assertTrue(task.projects.contains("+shopping"))
    }

    @Test
    fun project_with_hyphen(){
        val task = Task("2025-02-31 buy tofu +dinner-experiment")
        assertTrue(task.projects.contains("+dinner-experiment"))
    }

    @Test
    fun task_with_projects(){
        val task = Task("2025-02-31 buy tofu +shopping +dinner")
        assertTrue(task.projects.contains("+shopping"))
        assertTrue(task.projects.contains("+dinner"))
    }

    @Test
    fun task_with_project_in_middle(){
        val task = Task("2025-02-31 +project1 some more task info +project2 and some more stuff")
        assertTrue(task.projects.contains("+project1"))
        assertTrue(task.projects.contains("+project2"))
    }

    @Test
    fun task_with_context(){
        val task = Task("2025-02-31 buy tofu @shopping")
        assertTrue(task.contexts.contains("@shopping"))
    }

    @Test
    fun context_with_hyphen(){
        val task = Task("2025-02-31 buy tofu @running-errands")
        assertTrue(task.contexts.contains("@running-errands"))
    }

    @Test
    fun task_with_contexts(){
        val task = Task("2025-02-31 buy tofu @errands @wholefoods")
        assertTrue(task.contexts.contains("@errands"))
        assertTrue(task.contexts.contains("@wholefoods"))
    }

    @Test
    fun task_with_context_in_middle(){
        val task = Task("2025-02-31 @home some more task info @office and some more stuff")
        assertTrue(task.contexts.contains("@home"))
        assertTrue(task.contexts.contains("@office"))
    }


    @Test
    fun task_with_metadata(){
        val task = Task("2025-02-31 task data category:shopping")
        assertEquals("shopping", task.metadata["category"])
    }

    @Test
    fun task_with_multiple_metadata(){
        val task = Task("2025-02-31 task data category:shopping mood:optimistic")
        assertEquals("shopping", task.metadata["category"])
        assertEquals("optimistic", task.metadata["mood"])
    }

    @Test
    fun task_with_incorrect_metadata_format(){
        val task = Task("2025-02-31 task data category: shopping")
        assertEquals(null, task.metadata["category"])
    }

    @Test
    fun task_with_duplicate_metadata_keys_overwrites(){
        val task = Task("2025-02-31 task data category:shopping category:dinner")
        assertEquals("dinner", task.metadata["category"])
    }

    @Test
    fun task_due(){
        // The due metadata gets lifted to its own property for convenience
        val task = Task("2025-02-31 task data due:2025-03-14")
        assertEquals("2025-03-14", task.metadata["due"])
        assertEquals(LocalDate.of(2025, 3, 14), task.dueDate)
    }

    @Test
    fun task_due_not_in_body(){
        // If there's a valid due date, that metadata doesn't display in the task body
        val task = Task("2025-02-31 task data due:2025-03-14")
        assertEquals("task data", task.body)
    }

    @Test
    fun priority_none(){
        val pri = NoPriority
        assertEquals(" ", pri.display())
    }

    @Test
    fun priority_none_sorts_after_any_letter(){
        val none = NoPriority
        val priority = Priority('Z')

        assertTrue(none > priority)
        assertTrue(priority < none)
    }

    @Test
    fun priority_sorts_by_letter(){
        val priority1 = Priority('A')
        val priority2 = Priority('Z')

        assertTrue(priority2 > priority1)
        assertTrue(priority1 < priority2)
    }
}