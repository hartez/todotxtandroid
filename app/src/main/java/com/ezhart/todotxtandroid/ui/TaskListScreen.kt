package com.ezhart.todotxtandroid.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import com.ezhart.todotxtandroid.viewmodels.TasksViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(onNavigateToSettings: () -> Unit) {
    val viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory)
    val scope = rememberCoroutineScope()

    val uiState by viewModel.taskListUIState.collectAsStateWithLifecycle()
    val editorUIState by viewModel.editorUIState.collectAsStateWithLifecycle()
    val messageUIState = viewModel.messageUIState

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var isMenuSheetOpen by remember { mutableStateOf(false) }
    var isInTextFilterMode by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    LaunchedEffect(messageUIState) {
        if (messageUIState.pending) {
            scope.launch {
                val result = snackBarHostState
                    .showSnackbar(
                        message = messageUIState.text,
                        actionLabel = messageUIState.actionLabel,
                        duration = messageUIState.duration,
                        withDismissAction = messageUIState.duration == SnackbarDuration.Indefinite
                    )
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        messageUIState.action?.invoke()
                        messageUIState.onDismiss()
                    }

                    SnackbarResult.Dismissed -> {
                        messageUIState.onDismiss()
                    }
                }
            }
        }
    }

    BackHandler(uiState.filter != AllTasksFilter) {

        // TODO handle search bar being open

        viewModel.updateFilter(AllTasksFilter)
    }

    TodotxtAndroidTheme {
        Scaffold(
            snackbarHost = {
                SnackbarHost(hostState = snackBarHostState, snackbar = {
                    Snackbar(it, modifier = Modifier.padding(horizontal = 32.dp))
                })
            },
            contentWindowInsets = WindowInsets.statusBars,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Crossfade(
                    modifier = Modifier.animateContentSize(),
                    targetState = isInTextFilterMode,
                    label = "Search"
                ) { target ->
                    if (!target) {
                        AppBar(
                            { isFilterSheetOpen = true },
                            { isMenuSheetOpen = true },
                            showSearch = { isInTextFilterMode = true }
                        )
                    } else {
                        TextFilterBar(viewModel.textFilterEditor) {
                            isInTextFilterMode = false
                        }
                    }
                }
            },
            // TODO hide FAB when in text filter mode
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.editNewTask()
                    }
                ) {
                    Icon(Icons.Outlined.Add, "Add Task")
                }
            }
        ) { innerPadding ->

            // TODO Figure out how to stop pushing the task list up when the keyboard is showing
            // in text filter mode

            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = {
                    viewModel.loadTasks(true)
                }, modifier = Modifier.padding(innerPadding)
            ) {
                TaskList(
                    uiState.filteredTasks, uiState.filterLabel,
                    { viewModel.selectTask(it) },
                    onToggleCompleted = {
                        viewModel.toggleCompleted(it)
                    },
                    onEdit = {
                        viewModel.selectTask(it, false)
                        viewModel.editSelectedTask()
                    }
                )
            }

            FiltersSheet(
                uiState.allProjects,
                uiState.allContexts,
                isFilterSheetOpen,
                { isFilterSheetOpen = false },
                onUpdateFilter = viewModel::updateFilter,
                uiState.filter
            )

            MenuSheet(
                isMenuSheetOpen,
                { isMenuSheetOpen = false },
                onNavigateToSettings,
                { viewModel.loadTasks() })

            TaskEditor(
                editorUIState,
                {
                    viewModel.closeEditor()
                },
                {
                    viewModel.commitTaskChanges()
                },
                viewModel::listTagsSelections
            )

            if (viewModel.isDetailsOpen) {
                Dialog(onDismissRequest = { viewModel.dismissDetails() }) {
                    DetailsDialog(
                        { viewModel.dismissDetails() },
                        viewModel.selectedTask!!,
                        onEditRequest = { viewModel.editSelectedTask() },
                        onToggleCompleted = { viewModel.toggleCompleted(viewModel.selectedTask!!) })
                }
            }
        }
    }
}
