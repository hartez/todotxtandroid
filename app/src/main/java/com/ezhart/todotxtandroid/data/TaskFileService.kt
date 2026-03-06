package com.ezhart.todotxtandroid.data

import android.content.Context
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import kotlin.io.path.Path

class TaskFileService(val applicationContext: Context, settings: SettingsRepository) {

    val fileName = settings.todoPath.map { getFileName(it) }.stateIn(
        kotlinx.coroutines.MainScope(),
        SharingStarted.Eagerly,
        ""
    )

    fun loadTasksFromStorage(): List<Task> {

        // TODO error handling for missing file

        val file = File(applicationContext.filesDir, fileName.value)

        val lines = file.readLines()

        val tasks = mutableListOf<Task>()

        for (line in lines) {
            tasks.add(Task(line))
        }

        return tasks
    }

    fun getFileName(path: String): String {
        return Path(path).fileName.toString()
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
}