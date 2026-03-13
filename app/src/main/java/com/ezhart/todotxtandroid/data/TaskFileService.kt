package com.ezhart.todotxtandroid.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.last
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.Path

class TaskFileService(val applicationContext: Context, val settings: SettingsRepository) {

    suspend fun loadTasksFromStorage(): ReadTaskListResult = withContext(Dispatchers.IO) {
        try {
            val fileName = getFileName(getTodoPath())

            val file = File(applicationContext.filesDir, fileName)

            if (!file.exists()) {
                ReadTaskListResult.Error(FileNotFoundException(file.path))
            }

            val tasks = mutableListOf<Task>()

            file.readLines().mapTo(tasks) { Task(it) }

            ReadTaskListResult.Success(tasks)
        } catch (e: Exception) {
            ReadTaskListResult.Error(e)
        }
    }

    // TODO this should probably surface exceptions in a useful way
    suspend fun writeTasksToStorage(taskList: List<Task>) = withContext(Dispatchers.IO) {

        val fileName = getFileName(getTodoPath())

        val file = File(applicationContext.filesDir, fileName)

        val textList =
            taskList.joinToString(transform = { task -> task.task }, separator = "\n")

        file.writeText(textList)
    }

    suspend fun getTodoPath(): String {
        return settings.todoPath.take(1).last()
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

sealed interface ReadTaskListResult {
    class Success(val tasks: List<Task>) : ReadTaskListResult
    class Error(val e: Exception) : ReadTaskListResult
}

