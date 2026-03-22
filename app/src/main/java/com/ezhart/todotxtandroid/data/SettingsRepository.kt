package com.ezhart.todotxtandroid.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ezhart.todotxtandroid.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val DATASTORE_NAME = "app_settings"

class SettingsStorage(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = DATASTORE_NAME)

    private object PreferencesKeys {
        val ACCOUNT_DISPLAY_NAME = stringPreferencesKey("account_display_name")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val TODO_PATH = stringPreferencesKey("todo_path")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val accountDisplayName: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCOUNT_DISPLAY_NAME] ?: ""
    }

    val accountEmail: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACCOUNT_EMAIL] ?: ""
    }

    val todoPath: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.TODO_PATH] ?: "/tdtest/todo.txt"
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when(val themeMode = preferences[PreferencesKeys.THEME_MODE]){
            null -> ThemeMode.System
            "" -> ThemeMode.System
            else -> enumValueOf<ThemeMode>(themeMode)
        }
    }

    suspend fun setAccountDisplayName(accountDisplayName: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCOUNT_DISPLAY_NAME] = accountDisplayName
        }
    }

    suspend fun setAccountEmail(accountEmail: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCOUNT_EMAIL] = accountEmail
        }
    }

    suspend fun setTodoPath(todoPath: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TODO_PATH] = todoPath
        }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.toString()
        }
    }
}

class SettingsRepository(
    private val settingsStorage: SettingsStorage
) {
    val accountDisplayName: Flow<String> = settingsStorage.accountDisplayName
    val accountEmail: Flow<String> = settingsStorage.accountEmail
    val todoPath: Flow<String> = settingsStorage.todoPath
    val themeMode: Flow<ThemeMode> = settingsStorage.themeMode

    suspend fun setAccountDisplayName(accountDisplayName: String) {
        settingsStorage.setAccountDisplayName(accountDisplayName)
    }

    suspend fun setAccountEmail(accountEmail: String) {
        settingsStorage.setAccountEmail(accountEmail)
    }

    suspend fun setTodoPath(todoPath: String) {
        settingsStorage.setTodoPath(todoPath)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        settingsStorage.setThemeMode(mode)
    }
}

