package com.ezhart.todotxtandroid

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.data.CompletedFilter
import com.ezhart.todotxtandroid.data.ContextFilter
import com.ezhart.todotxtandroid.data.DueFilter
import com.ezhart.todotxtandroid.data.PendingFilter
import com.ezhart.todotxtandroid.data.ProjectFilter
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(onNavigateToSettings: () -> Unit) {
    val tasks = generateFakeTasks(100)

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var isNavSheetOpen by remember { mutableStateOf(false) }

    var filter by remember { mutableStateOf<Any>(AllTasksFilter) }

    TodotxtAndroidTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                AppBar(
                    { isFilterSheetOpen = true },
                    { isNavSheetOpen = true }
                )
            }
        ) { innerPadding ->
            TaskList(
                filterTasks(tasks, filter), header(filter),
                { },
                modifier = Modifier
                    .padding(innerPadding)
            )

            FiltersSheet(
                allProjects(tasks),
                isFilterSheetOpen,
                { isFilterSheetOpen = false },
                onUpdateFilter = { f -> filter = f },
                filter
            )

            NavSheet(isNavSheetOpen, { isNavSheetOpen = false }, onNavigateToSettings)
        }
    }
}

fun header(filter: Any): String {
    return when (filter) {
        is ProjectFilter -> "Project ${filter.project}"
        is DueFilter -> "Due Tasks"
        is ContextFilter -> "Context ${filter.context}"
        is PendingFilter -> "Pending Tasks"
        is CompletedFilter -> "Completed Tasks"
        else -> "All Tasks"
    }
}

fun filterTasks(tasks: List<Task>, filter: Any): List<Task> {
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

fun allProjects(tasks: List<Task>): List<String> {
    return tasks.flatMap({ t -> t.projects }).distinct().sorted()
}

fun generateFakeTasks(count: Int): List<Task> {
    val x = mutableListOf<Task>()
    for (n in 0..count) {
        if (n % 9 == 0) {
            x.add(Task("x 2026-02-01 Task $n +shopping"))
        } else if (n % 5 == 0) {
            x.add(Task("Task $n @testContext"))
        } else if (n % 4 == 0) {
            x.add(Task("Task @testContext2 +project2"))
        } else {
            x.add(Task("Task $n"))
        }
    }

    return x
}

@Preview(name = "TaskList Screen", showBackground = true)
@Composable
fun TaskListScreenPreview() {
    TodotxtAndroidTheme {
        TaskListScreen { }
    }
}