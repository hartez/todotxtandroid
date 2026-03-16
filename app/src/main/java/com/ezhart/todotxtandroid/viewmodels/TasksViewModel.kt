package com.ezhart.todotxtandroid.viewmodels

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ezhart.todotxtandroid.TAG
import com.ezhart.todotxtandroid.TodotxtAndroidApplication
import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.data.CompletedFilter
import com.ezhart.todotxtandroid.data.ContextFilter
import com.ezhart.todotxtandroid.data.DueFilter
import com.ezhart.todotxtandroid.data.Filter
import com.ezhart.todotxtandroid.data.PendingFilter
import com.ezhart.todotxtandroid.data.ProjectFilter
import com.ezhart.todotxtandroid.data.ReadTaskListResult
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.data.TaskFileService
import com.ezhart.todotxtandroid.dropbox.DropboxService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

class TasksViewModel(
    private val taskFileService: TaskFileService,
    private val dropboxService: DropboxService,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    var isRefreshing by mutableStateOf(false)
        private set

    var selectedTask by mutableStateOf<Task?>(null)
        private set

    var isDetailsOpen by mutableStateOf(false)
        private set

    var alert by mutableStateOf<String?>(null)
        private set

    private var tasks: MutableStateFlow<MutableList<Task>> = MutableStateFlow(mutableStateListOf())
    private val filter = MutableStateFlow<Filter>(AllTasksFilter)

    val uiState: StateFlow<TaskListUIState> = combine(filter, tasks) { filter, tasks ->
        TaskListUIState(
            filterTasks(tasks, filter),
            filter,
            allContexts(tasks),
            allProjects(tasks)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUIState()
    )

    private val isEditorOpen = MutableStateFlow(false)
    private val newTaskEditor = TextFieldState()
    private val existingTaskEditor = TextFieldState()
    private var editorMode: TaskEditorMode = TaskEditorMode.Create

    val editorUIState: StateFlow<TaskEditorUIState> = isEditorOpen.map { it ->
        TaskEditorUIState(
            it, editorMode, when (editorMode) {
                TaskEditorMode.Create -> newTaskEditor
                TaskEditorMode.Edit -> existingTaskEditor
            }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = TaskEditorUIState(
            isEditorOpen.value,
            editorMode,
            newTaskEditor
        )
    )

    fun listTagsSelections(task: String): Map<String, Boolean> {
        val selectedContexts = Task.parseContexts(task)
        val selectedProjects = Task.parseProjects(task)

        val all = allProjects(tasks.value) + allContexts(tasks.value)

        return all.associateWith { tag ->
            (selectedContexts.contains(tag) || selectedProjects.contains(tag))
        }
    }

    fun selectTask(task: Task) {
        selectedTask = task
        showDetails()
    }

    fun clearTaskSelection() {
        selectedTask = null
    }

    fun showDetails() {
        isDetailsOpen = true
    }

    fun dismissDetails() {
        isDetailsOpen = false
        clearTaskSelection()
    }

    fun updateFilter(newFilter: Filter) {
        filter.value = newFilter
    }

    fun editSelectedTask() {
        isDetailsOpen = false

        val taskText = Task.removeCreatedDate(selectedTask!!.task)

        existingTaskEditor.setTextAndPlaceCursorAtEnd(taskText)
        editorMode = TaskEditorMode.Edit
        isEditorOpen.value = true
    }

    fun editNewTask() {
        editorMode = TaskEditorMode.Create
        isEditorOpen.value = true
    }

    fun closeEditor() {
        isEditorOpen.value = false
    }

    fun toggleCompleted(task: Task) {

        val selected = task == selectedTask

        val updateTaskText =
            if (task.completed) {
                Task.markPending(task.task)
            } else {
                Task.markCompleted(task.task, LocalDate.now())
            }

        val updatedTask = editTask(task, updateTaskText)

        if (selected) {
            selectedTask = updatedTask
        }
    }

    fun commitTaskChanges() {
        if (editorMode == TaskEditorMode.Create) {
            val toAdd = newTaskEditor.text.toString()

            newTaskEditor.clearText()

            addTask(toAdd)
        } else {
            val oldTask = selectedTask!!
            val updatedTask = existingTaskEditor.text.toString()

            existingTaskEditor.clearText()
            isEditorOpen.value = false
            clearTaskSelection()
            editorMode = TaskEditorMode.Create

            editTask(oldTask, updatedTask)
        }
    }

    fun showAlert(message: String) {
        alert = message
    }

    fun clearAlert() {
        alert = null
    }

    fun loadTasks(shouldSync: Boolean = false) {
        viewModelScope.launch {
            isRefreshing = true

            if (shouldSync) {
                dropboxService.sync()
            }

            when (val result = taskFileService.loadTasksFromStorage()) {
                is ReadTaskListResult.Success -> tasks.value = result.tasks.toMutableList()
                is ReadTaskListResult.Error -> {
                    tasks.value = mutableListOf()
                    Log.e(TAG, result.e.toString())

                    showAlert("Error reading tasks from local storage")
                }
            }


            // TODO this is a hack, got to figure out how to fix this
            // if the update is too fast, the refreshing state will get stuck
            delay(100)

            isRefreshing = false
        }
    }

    private fun filterTasks(tasks: List<Task>, filter: Filter): List<Task> {
        val result = when (filter) {
            is ProjectFilter -> tasks.filter { t -> t.projects.contains(filter.project) }
            is ContextFilter -> tasks.filter { t -> t.contexts.contains(filter.context) }
            is DueFilter -> tasks.filter { t -> t.dueDate != null }
            is PendingFilter -> tasks.filter { t -> !t.completed }
            is CompletedFilter -> tasks.filter { t -> t.completed }
            else -> tasks
        }

        return result.distinct().sortedWith(compareBy(Task::taskPriority, Task::completed))
    }

    private fun allProjects(tasks: List<Task>): List<String> {
        return tasks.flatMap { t -> t.projects }.distinct().sorted()
    }

    private fun allContexts(tasks: List<Task>): List<String> {
        return tasks.flatMap { t -> t.contexts }.distinct().sorted()
    }

    private fun addTask(task: String) {
        // Make sure the created date is in the task
        val taskText = Task.insertCreatedDate(task, LocalDate.now())

        tasks.update {
            tasks.value.toMutableList().apply {
                this.add(Task(taskText))
            }
        }

        viewModelScope.launch {
            taskFileService.writeTasksToStorage(tasks.value)
        }
    }

    private fun editTask(task: Task, updated: String): Task {
        val taskText =
            when (val created = task.createdDate) {
                null -> updated
                else -> Task.insertCreatedDate(updated, created)
            }

        val updatedTask = Task(taskText)

        tasks.update {
            tasks.value.toMutableList().apply {
                this[this.indexOf(task)] = updatedTask
            }
        }

        viewModelScope.launch {
            taskFileService.writeTasksToStorage(tasks.value)
        }

        return updatedTask
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()
                val dropboxService =
                    (this[APPLICATION_KEY] as TodotxtAndroidApplication).dropboxService
                val taskFileService =
                    (this[APPLICATION_KEY] as TodotxtAndroidApplication).taskFileService
                TasksViewModel(
                    taskFileService = taskFileService,
                    dropboxService = dropboxService,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}