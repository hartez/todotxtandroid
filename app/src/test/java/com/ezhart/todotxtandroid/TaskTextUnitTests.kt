package com.ezhart.todotxtandroid

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
        val updatedTask = Task.editTags(body = task, project)

        assertTrue(Task(updatedTask).projects.contains(project))
    }

    @Test
    fun add_tags(){
        val task = "This is a test task"
        val project = "+project"
        val context = "@context"
        val updatedTaskText = Task.editTags(body = task, project, context)
        val updatedTask = Task(updatedTaskText)

        assertTrue(updatedTask.projects.contains(project))
        assertTrue(updatedTask.contexts.contains(context))
    }

    @Test
    fun replace_tag(){
        val task = "This is a test task +oldProject"
        val project = "+project"
        val updatedTaskText = Task.editTags(body = task, project)
        val updatedTask = Task(updatedTaskText)

        assertTrue(updatedTask.projects.contains(project))
        assertFalse(updatedTask.projects.contains("+oldProject"))
    }

    @Test
    fun replace_tags(){
        val task = "This is a test @oldContext task +oldProject"
        val project = "+project"
        val context = "@context"
        val updatedTaskText = Task.editTags(body = task, context, project)
        val updatedTask = Task(updatedTaskText)

        assertTrue(updatedTask.projects.contains(project))
        assertFalse(updatedTask.projects.contains("+oldProject"))

        assertTrue(updatedTask.contexts.contains(context))
        assertFalse(updatedTask.contexts.contains("@oldContext"))
    }
}