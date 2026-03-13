package com.ezhart.todotxtandroid

import androidx.activity.compose.LocalActivity
import com.ezhart.todotxtandroid.data.Priority
import com.ezhart.todotxtandroid.data.None
import com.ezhart.todotxtandroid.data.Task
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskTextUnitTests {

    @Test
    fun add_priority() {
        val task = "This is a test task"

        val updatedTask = Task.editPriority(task, Priority('A'))

        assertEquals("(A) This is a test task", updatedTask)
        assertEquals(Priority('A'), Task.parsePriority(updatedTask))
    }

    @Test
    fun remove_priority() {
        val task = "(A) This is a test task"

        val updatedTask = Task.editPriority(task, None)

        assertEquals("This is a test task", updatedTask)
        assertEquals(None, Task.parsePriority(updatedTask))
    }

    @Test
    fun add_due_date() {
        val task = "This is a test task"
        val dueDate = LocalDate.of(2025, 2, 3)
        val updatedTask = Task.editDueDate(task, dueDate)
        assertEquals("This is a test task due:2025-02-03", updatedTask)
    }

    @Test
    fun update_due_date() {
        val task = "This is a test task due:2025-09-08"
        val dueDate = LocalDate.of(2025, 2, 3)
        val updatedTask = Task.editDueDate(task, dueDate)
        assertEquals("This is a test task due:2025-02-03", updatedTask)
    }

    @Test
    fun add_tag(){
        val task = "This is a test task"
        val project = "+project"
        val updatedTask = Task.editTags(task = task, project)

        assertTrue(Task(updatedTask).projects.contains(project))
    }

    @Test
    fun add_tags(){
        val task = "This is a test task"
        val project = "+project"
        val context = "@context"
        val updatedTaskText = Task.editTags(task = task, project, context)
        val updatedTask = Task(updatedTaskText)

        assertTrue(updatedTask.projects.contains(project))
        assertTrue(updatedTask.contexts.contains(context))
    }

    @Test
    fun replace_tag(){
        val task = "This is a test task +oldProject"
        val project = "+project"
        val updatedTaskText = Task.editTags(task = task, project)
        val updatedTask = Task(updatedTaskText)

        assertTrue(updatedTask.projects.contains(project))
        assertFalse(updatedTask.projects.contains("+oldProject"))
    }

    @Test
    fun replace_tags(){
        val task = "This is a test @oldContext task +oldProject"
        val project = "+project"
        val context = "@context"
        val updatedTaskText = Task.editTags(task = task, context, project)
        val updatedTask = Task(updatedTaskText)

        assertTrue(updatedTask.projects.contains(project))
        assertFalse(updatedTask.projects.contains("+oldProject"))

        assertTrue(updatedTask.contexts.contains(context))
        assertFalse(updatedTask.contexts.contains("@oldContext"))
    }

    @Test
    fun add_created_date_to_input(){
        val task = "This is a test"
        val date = LocalDate.of(2025, 10, 7)
        val updatedTaskText = Task.insertCreatedDate(task, date)

        assertEquals( "2025-10-07 This is a test", updatedTaskText)

        val updatedTask = Task(updatedTaskText)
        assertEquals(date, updatedTask.createdDate)
    }

    @Test
    fun add_created_date_to_input_with_priority(){
        val task = "(B) This is a test"
        val date = LocalDate.of(2025, 10, 7)
        val updatedTaskText = Task.insertCreatedDate(task, date)

        assertEquals( "(B) 2025-10-07 This is a test", updatedTaskText)

        val updatedTask = Task(updatedTaskText)
        assertEquals(date, updatedTask.createdDate)
    }

    @Test
    fun add_created_date_to_input_with_created_date(){
        val task = "2024-11-07 This is a test"
        val date = LocalDate.of(2025, 10, 7)
        val updatedTaskText = Task.insertCreatedDate(task, date)

        // If the task already has a created date, leave it alone
        assertEquals( "2024-11-07 This is a test", updatedTaskText)
    }

    @Test
    fun add_created_date_to_completed_task_input(){
        val task = "x 2026-05-05 This is a test"
        val date = LocalDate.of(2025, 10, 7)
        val updatedTaskText = Task.insertCreatedDate(task, date)

        assertEquals( "x 2026-05-05 2025-10-07 This is a test", updatedTaskText)

        val updatedTask = Task(updatedTaskText)
        assertEquals(date, updatedTask.createdDate)
    }

    @Test
    fun remove_created_date() {
        val task = "2026-05-05 This is a test"
        val updatedTask = Task.removeCreatedDate(task)

        assertEquals( "This is a test", updatedTask)
    }

    @Test
    fun remove_created_date_when_it_is_not_present() {
        val task = "This is a test"
        val updatedTask = Task.removeCreatedDate(task)

        assertEquals( "This is a test", updatedTask)
    }

    @Test
    fun remove_created_date_but_not_due_date() {
        val task = "2026-05-05 This is a test due:2026-05-05"
        val updatedTask = Task.removeCreatedDate(task)

        assertEquals( "This is a test due:2026-05-05", updatedTask)
    }

    @Test
    fun remove_created_date_from_task_with_priority() {
        val task = "(A) 2026-05-05 This is a test"
        val updatedTask = Task.removeCreatedDate(task)

        assertEquals( "(A) This is a test", updatedTask)
    }

    @Test
    fun remove_created_date_from_completed_task() {
        val task = "x 2026-05-06 2026-05-05 This is a test"
        val updatedTask = Task.removeCreatedDate(task)

        assertEquals( "x 2026-05-06 This is a test", updatedTask)
    }

    @Test
    fun mark_completed() {
        val task = "2026-05-05 This is a test"
        val completedDate = LocalDate.of(2026, 5, 6)
        val updatedTask = Task.markCompleted(task, completedDate)

        assertEquals( "x 2026-05-06 2026-05-05 This is a test", updatedTask)
    }

    @Test
    fun mark_completed_already_complete() {
        val task = "x 2026-05-06 2026-05-05 This is a test"
        val completedDate = LocalDate.of(2026, 5, 6)
        val updatedTask = Task.markCompleted(task, completedDate)

        // Since the task is already complete, the update should be ignored. The only way to
        // update the completed date is to mark the task pending and _then_ mark it completed again
        assertEquals( "x 2026-05-06 2026-05-05 This is a test", updatedTask)
    }

    @Test
    fun mark_pending() {
        val task = "x 2026-05-06 2026-05-05 This is a test"
        val updatedTask = Task.markPending(task)

        assertEquals( "2026-05-05 This is a test", updatedTask)
    }

    @Test
    fun mark_pending_already_pending() {
        val task = "2026-05-05 This is a test"
        val updatedTask = Task.markPending(task)

        assertEquals( "2026-05-05 This is a test", updatedTask)
    }
}