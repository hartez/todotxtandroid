package com.ezhart.todotxtandroid.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.ezhart.todotxtandroid.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

const val APP_SETTINGS_STORE_NAME = "app_settings"

class SettingsRepository(private val context: Context) {

    private val Context.dataStore by preferencesDataStore(name = APP_SETTINGS_STORE_NAME)

    private object PreferencesKeys {
        val ACCOUNT_DISPLAY_NAME = stringPreferencesKey("account_display_name")
        val ACCOUNT_EMAIL = stringPreferencesKey("account_email")
        val TODO_PATH = stringPreferencesKey("todo_path")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val DYNAMIC_COLOR = booleanPreferencesKey("dynamic_color")
        val SYNC_ON_START = booleanPreferencesKey("sync_on_start")
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
        when (val themeMode = preferences[PreferencesKeys.THEME_MODE]) {
            null -> ThemeMode.System
            "" -> ThemeMode.System
            else -> enumValueOf<ThemeMode>(themeMode)
        }
    }

    val useDynamicColor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.DYNAMIC_COLOR] ?: false
    }

    val syncOnStart: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SYNC_ON_START] ?: false
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

    suspend fun setUseDynamicColor(useDynamicColor: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DYNAMIC_COLOR] = useDynamicColor
        }
    }

    suspend fun setSyncOnStart(syncOnStart: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SYNC_ON_START] = syncOnStart
        }
    }
}

