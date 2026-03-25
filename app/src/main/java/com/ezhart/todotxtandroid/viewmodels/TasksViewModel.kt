package com.ezhart.todotxtandroid.viewmodels

import android.util.Log
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import com.ezhart.todotxtandroid.data.SettingsRepository
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.data.TaskFileService
import com.ezhart.todotxtandroid.dropbox.DropboxService
import com.ezhart.todotxtandroid.dropbox.SyncResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.time.LocalDate

class TasksViewModel(
    private val taskFileService: TaskFileService,
    private val dropboxService: DropboxService,
    private val settingsRepository: SettingsRepository,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    var startupLoaded = false

    var isRefreshing by mutableStateOf(false)
        private set

    private val selectedTask = MutableStateFlow<Task?>(null)

    private val isDetailsOpen = MutableStateFlow(false)

    var messageUIState by mutableStateOf(MessageUIState())
        private set

    val textFilterEditor = TextFieldState()

    private var tasks: MutableStateFlow<MutableList<Task>> = MutableStateFlow(mutableStateListOf())
    private val filter = MutableStateFlow<Filter>(AllTasksFilter)
    private val textFilter = snapshotFlow { textFilterEditor.text }

    val taskListUIState: StateFlow<TaskListUIState> =
        combine(filter, tasks, textFilter) { filter, tasks, textFilter ->
            TaskListUIState(
                filterTasks(tasks, filter, textFilter),
                filter,
                textFilter,
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

    val editorUIState: StateFlow<TaskEditorUIState> = isEditorOpen.map {
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

    val detailsDialogUIState: StateFlow<DetailsDialogUIState> = combine(isDetailsOpen, selectedTask) {
        isDetailsOpen, selectedTask ->
        DetailsDialogUIState(
            isDetailsOpen,
            selectedTask,
            nextTask(),
            previousTask(),
            { dismissDetails() },
            { t -> selectTask(t) },
            {editSelectedTask()},
            { if(selectedTask != null){ toggleCompleted(selectedTask) }}
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = DetailsDialogUIState()
    )

    fun listTagsSelections(task: String): Map<String, Boolean> {
        val selectedContexts = Task.parseContexts(task)
        val selectedProjects = Task.parseProjects(task)

        val all = allProjects(tasks.value) + allContexts(tasks.value)

        return all.associateWith { tag ->
            (selectedContexts.contains(tag) || selectedProjects.contains(tag))
        }
    }

    fun selectTask(task: Task, showDetails: Boolean = true) {
        selectedTask.value = task
        if (showDetails) {
            showDetails()
        }
    }

    private fun nextTask() : Task? {
        val currentTask = selectedTask.value ?: return null
        val tasks = taskListUIState.value.filteredTasks

        val currentIndex = tasks.indexOf(currentTask)
        if(currentIndex >= tasks.count() - 1){
            return tasks.first()
        }

        return tasks[currentIndex + 1]
    }

    private fun previousTask() : Task? {
        val currentTask = selectedTask.value ?: return null
        val tasks = taskListUIState.value.filteredTasks

        val currentIndex = tasks.indexOf(currentTask)

        if(currentIndex == 0){
            return tasks.last()
        }

        return tasks[currentIndex - 1]
    }

    fun clearTaskSelection() {
        selectedTask.value = null
    }

    fun showDetails() {
        isDetailsOpen.value = true
    }

    fun dismissDetails() {
        isDetailsOpen.value = false
        clearTaskSelection()
    }

    fun updateFilter(newFilter: Filter) {
        filter.value = newFilter
    }

    fun back() {
        unwindFilter()
    }

    fun editSelectedTask() {
        isDetailsOpen.value = false

        val taskText = Task.removeCreatedDate(selectedTask.value!!.task)

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

        val selected = task == selectedTask.value

        val message = when (task.completed) {
            true -> "Task marked pending"
            false -> "Task marked complete"
        }

        val updateTaskText =
            if (task.completed) {
                Task.markPending(task.task)
            } else {
                Task.markCompleted(task.task, LocalDate.now())
            }

        val updatedTask = editTask(task, updateTaskText)

        if (selected) {
            selectedTask.value = updatedTask
        }

        showActionAlert(message, "Undo") { toggleCompleted(updatedTask) }
    }

    fun commitTaskChanges(markComplete: Boolean) {
        when (editorMode) {
            TaskEditorMode.Create -> createTask(markComplete)
            TaskEditorMode.Edit -> updateSelectedTask()
        }
    }

    fun showAlert(message: String) {
        messageUIState = MessageUIState(
            pending = true,
            message,
            { clearAlert() }
        )
    }

    fun showError(message: String) {
        messageUIState = MessageUIState(
            pending = true,
            message,
            { clearAlert() },
            duration = SnackbarDuration.Indefinite
        )
    }

    fun showActionAlert(message: String, actionLabel: String, action: () -> Unit) {
        messageUIState = MessageUIState(
            pending = true,
            text = message,
            actionLabel = actionLabel,
            action = action,
            onDismiss = { clearAlert() }
        )
    }

    fun clearAlert() {
        messageUIState = MessageUIState(pending = false)
    }

    fun loadTasksAtStartup() {
        if (startupLoaded) return

        viewModelScope.launch {
            startupLoaded = true
            loadTasks(settingsRepository.syncOnStart.first())
        }
    }

    fun loadTasks(shouldSync: Boolean = true) {
        viewModelScope.launch {
            isRefreshing = true

            if (shouldSync) {
                when(val syncResult = dropboxService.sync()){
                    is SyncResult.NotConnected -> {
                        Log.i(TAG, "No network connection, skipping remote sync.")
                        showAlert("No network connection")
                    }
                    is SyncResult.Success -> {
                        Log.i(TAG, syncResult.message)
                    }
                    is SyncResult.Conflict -> showAlert(syncResult.message)
                    is SyncResult.Error -> showError(syncResult.e.message.toString())
                    is SyncResult.NotAuthenticated -> {
                        Log.i(TAG, "Not authenticated to Dropbox, skipping remote sync.")
                        showAlert("Not authenticated to Dropbox")
                    }
                }
            }

            when (val result = taskFileService.loadTasksFromStorage()) {
                is ReadTaskListResult.Success -> tasks.value = result.tasks.toMutableList()
                is ReadTaskListResult.Error -> {
                    tasks.value = mutableListOf()

                    when(result.e){
                        is FileNotFoundException -> Log.e(TAG, result.e.toString())
                        else ->  showError("Error reading tasks from local storage: ${result.e.message}")
                    }
                }
            }


            // TODO this is a hack, got to figure out how to fix this
            // if the update is too fast, the refreshing state will get stuck
            delay(100)

            isRefreshing = false
        }
    }

    /* For the moment, we're just ignoring blank task updates entirely.
    In the future it may make sense to consider them invalid input in the UI
    or to use "blanking" a task as a way to delete it. But for now I don't have a
    string opinion on which it should be, since it's not part of my usage pattern. */

    private fun createTask(markComplete: Boolean) {
        var toAdd = newTaskEditor.text.toString()
        newTaskEditor.clearText()

        if (toAdd.isBlank()) {
            return
        }

        if (markComplete) {
            toAdd = Task.markCompleted(toAdd, LocalDate.now())
        }

        addTask(toAdd)
    }

    private fun updateSelectedTask() {
        val oldTask = selectedTask.value!!
        val updatedTask = existingTaskEditor.text.toString()

        existingTaskEditor.clearText()
        isEditorOpen.value = false
        clearTaskSelection()
        editorMode = TaskEditorMode.Create

        if (updatedTask.isBlank()) {
            return
        }

        editTask(oldTask, updatedTask)
    }

    private fun unwindFilter() {
        if (!textFilterEditor.text.isEmpty()) {
            textFilterEditor.clearText()
            return
        }

        if (filter != AllTasksFilter) {
            updateFilter(AllTasksFilter)
        }
    }

    private fun filterTasks(
        tasks: List<Task>,
        filter: Filter,
        textFilter: CharSequence
    ): List<Task> {
        var result = when (filter) {
            is ProjectFilter -> tasks.filter { t -> t.projects.contains(filter.project) }
            is ContextFilter -> tasks.filter { t -> t.contexts.contains(filter.context) }
            is DueFilter -> tasks.filter { t -> t.dueDate != null }
            is PendingFilter -> tasks.filter { t -> !t.completed }
            is CompletedFilter -> tasks.filter { t -> t.completed }
            else -> tasks
        }

        if (!textFilter.isBlank()) {
            result = result.filter { t -> t.body.contains(textFilter, ignoreCase = true) }
        }

        // TODO Filter out blank lines (is that a totally blank Task? There might accidentally be
        // blank lines in the source task file; instead of crashing, we could have a property on
        // Task like "isEmpty" and then ignore it here. Probably ignore it when we write the task
        // file back, too.

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

        showAlert("Task created")
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
                val settingsRepository =
                    (this[APPLICATION_KEY] as TodotxtAndroidApplication).settingsRepository
                TasksViewModel(
                    taskFileService = taskFileService,
                    dropboxService = dropboxService,
                    settingsRepository = settingsRepository,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}