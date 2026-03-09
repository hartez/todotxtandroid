package com.ezhart.todotxtandroid.viewmodels

import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.data.Filter
import com.ezhart.todotxtandroid.data.Task

data class TaskListUIState(
    val filteredTasks: List<Task> = listOf(),
    val filter: Filter = AllTasksFilter,
    val allContexts: List<String> = listOf(),
    val allProjects: List<String> = listOf()
) {
    val filterLabel = filter.display()
}