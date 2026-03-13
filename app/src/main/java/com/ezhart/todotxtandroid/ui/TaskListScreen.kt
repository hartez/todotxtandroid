package com.ezhart.todotxtandroid.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import com.ezhart.todotxtandroid.viewmodels.TasksViewModel

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(onNavigateToSettings: () -> Unit) {

    // TODO new tasks aren't showing up at the end of the list right away; have to scroll up a bit and back down
    // Something with the tasks stateflow isn't working as expected

    val viewModel: TasksViewModel = viewModel(factory = TasksViewModel.Factory)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val editorUIState by viewModel.editorUIState.collectAsStateWithLifecycle()

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var isMenuSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTasks()
    }

    TodotxtAndroidTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                AppBar(
                    { isFilterSheetOpen = true },
                    { isMenuSheetOpen = true }
                )

            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        viewModel.editNewTask()
                    },
                ) {
                    Icon(Icons.Outlined.Add, "Add Task")
                }
            }
        ) { innerPadding ->

            PullToRefreshBox(
                isRefreshing = viewModel.isRefreshing,
                onRefresh = {
                    viewModel.loadTasks(true)
                }, modifier = Modifier.padding(innerPadding)
            ) {
                TaskList(
                    uiState.filteredTasks, uiState.filterLabel,
                    { viewModel.selectTask(it) }
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
                }
            )

            if (viewModel.alert != null) {
                BasicAlertDialog({ viewModel.clearAlert() }) {
                    Text(viewModel.alert ?: "")
                }
            }

            if (viewModel.isDetailsOpen) {
                Dialog(onDismissRequest = { viewModel.dismissDetails() }) {
                    DetailsDialog(
                        { viewModel.dismissDetails() },
                        viewModel.selectedTask!!,
                        onEditRequest = { viewModel.editSelectedTask() },
                        onToggleCompleted = { viewModel.toggleCompleted() })
                }
            }
        }
    }
}

