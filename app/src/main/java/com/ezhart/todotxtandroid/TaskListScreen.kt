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
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskListScreen(onNavigateToSettings: () -> Unit) {
    val t = generateFakeTasks(100)

    var isFilterSheetOpen by remember { mutableStateOf(false) }
    var isNavSheetOpen by remember { mutableStateOf(false) }

    TodotxtAndroidTheme {
        Scaffold(
            contentWindowInsets = WindowInsets.statusBars,
            modifier = Modifier
                .fillMaxSize(),
            bottomBar = {
                AppBar(
                    {isFilterSheetOpen = true},
                    {isNavSheetOpen = true}
                )
            }
        ) { innerPadding ->
            TaskList(
                t, { _ -> },
                modifier = Modifier
                    .padding(innerPadding)
            )

            FiltersSheet(isFilterSheetOpen) { isFilterSheetOpen = false }
            NavSheet(isNavSheetOpen, {isNavSheetOpen = false}, onNavigateToSettings)
        }
    }
}

fun generateFakeTasks(count: Int) : List<Task>{
    val x = mutableListOf<Task>()
    for (n in 0..count){
        x.add(Task("Task $n"))
    }

    return x
}

@Preview(name = "TaskList Screen", showBackground = true)
@Composable
fun TaskListScreenPreview() {
    TodotxtAndroidTheme {
        TaskListScreen {  }
    }
}