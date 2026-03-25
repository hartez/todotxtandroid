package com.ezhart.todotxtandroid.data

import android.content.Context
import com.ezhart.todotxtandroid.dropbox.SyncDataStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import kotlin.io.path.Path

class TaskFileService(
    val applicationContext: Context,
    val settings: SettingsRepository,
    val syncData: SyncDataStorage
) {
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
        syncData.updateCurrentLocalRevision()
    }

    suspend fun getTodoPath(): String {
        return settings.todoPath.first()
    }

    fun getFileName(path: String): String {
        return Path(path).fileName.toString()
    }
}

sealed interface ReadTaskListResult {
    class Success(val tasks: List<Task>) : ReadTaskListResult
    class Error(val e: Exception) : ReadTaskListResult
}

