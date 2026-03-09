package com.ezhart.todotxtandroid.data

sealed class Filter{
    fun display(): String{
        return when (this) {
            is ProjectFilter -> "Project ${this.project}"
            is DueFilter -> "Due Tasks"
            is ContextFilter -> "Context ${this.context}"
            is PendingFilter -> "Pending Tasks"
            is CompletedFilter -> "Completed Tasks"
            else -> "All Tasks"
        }
    }
}

data object AllTasksFilter : Filter()
data object DueFilter : Filter()
data class ProjectFilter(val project: String) : Filter()
data class ContextFilter(val context: String) : Filter()
data object CompletedFilter : Filter()
data object PendingFilter : Filter()