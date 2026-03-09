package com.ezhart.todotxtandroid.viewmodels

import android.util.Log
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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TasksViewModel(
    private val taskFileService: TaskFileService,
    private val dropboxService: DropboxService,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    var tasks: MutableStateFlow<MutableList<Task>> = MutableStateFlow(mutableStateListOf())
    val filter = MutableStateFlow<Filter>(AllTasksFilter)
    var isRefreshing by mutableStateOf(false)
    var alert by mutableStateOf<String?>(null)

    val uiState: StateFlow<TaskListUIState> = combine(filter, tasks) { filter1, tasks ->
        TaskListUIState(
            filterTasks(tasks, filter1),
            filter1,
            allContexts(tasks),
            allProjects(tasks)
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        initialValue = TaskListUIState()
    )

    fun updateFilter(newFilter: Filter) {
        filter.value = newFilter
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

        return result
    }

    private fun allProjects(tasks: List<Task>): List<String> {
        return tasks.flatMap { t -> t.projects }.distinct().sorted()
    }

    private fun allContexts(tasks: List<Task>): List<String> {
        return tasks.flatMap { t -> t.contexts }.distinct().sorted()
    }

    fun showAlert(message: String){
        alert = message
    }

    fun clearAlert(){
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