package com.ezhart.todotxtandroid.viewmodels

import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.data.Filter
import com.ezhart.todotxtandroid.data.Task

data class TaskListUIState(
    val filteredTasks: List<Task> = listOf(),
    val filter: Filter = AllTasksFilter,
    val textFilter: CharSequence = "",
    val allContexts: List<String> = listOf(),
    val allProjects: List<String> = listOf()
) {
    val filterLabel = filter.display() // TODO this method needs to be updated to show the filter text stuff
}

