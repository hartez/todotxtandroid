package com.ezhart.todotxtandroid

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.serialization.Serializable

@Serializable
object Tasks

@Serializable
object Settings


@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Tasks) {
        composable<Tasks> {
            TaskListScreen({ navController.navigate(route = Settings) })
        }

        composable<Settings> {
            SettingsScreen()
        }
    }
}