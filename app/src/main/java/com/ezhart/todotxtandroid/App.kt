package com.ezhart.todotxtandroid

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ezhart.todotxtandroid.data.SettingsRepository
import com.ezhart.todotxtandroid.data.SettingsStorage
import com.ezhart.todotxtandroid.data.TaskFileService
import com.ezhart.todotxtandroid.dropbox.DropboxService
import kotlinx.serialization.Serializable

@Serializable
data object Tasks
@Serializable
object Settings

@Composable
fun App() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Tasks) {
        composable<Tasks> {
            TaskListScreen { navController.navigate(route = Settings) }
        }

        composable<Settings> {
            SettingsScreen()
        }
    }
}

class TodotxtAndroidApplication : Application() {
    val settingsRepository: SettingsRepository by lazy { SettingsRepository(SettingsStorage(this)) }
    val taskFileService: TaskFileService by lazy { TaskFileService(this, settingsRepository) }
    val dropboxService: DropboxService by lazy { DropboxService(this, settingsRepository) }
}