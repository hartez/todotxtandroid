package com.ezhart.todotxtandroid.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ezhart.todotxtandroid.TodotxtAndroidApplication
import com.ezhart.todotxtandroid.data.SettingsRepository
import com.ezhart.todotxtandroid.dropbox.DropboxService
import com.ezhart.todotxtandroid.dropbox.GetCurrentAccountResult
import com.ezhart.todotxtandroid.ui.theme.ThemeMode
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
    private val dropboxService: DropboxService,
    private val savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    var isSignedIn by mutableStateOf(dropboxService.isAuthenticated())
    var accountName = settingsRepository.accountDisplayName
    var accountEmail = settingsRepository.accountEmail
    var todoPath = settingsRepository.todoPath
    var themeMode = settingsRepository.themeMode

    fun beginSignIn(context: Context) {
        dropboxService.signIn(context) {
            isSignedIn = dropboxService.isAuthenticated()
            updateAccountInfo()
        }
    }

    fun signOut() {
        dropboxService.signOut()
        isSignedIn = dropboxService.isAuthenticated()
        updateAccountInfo()
    }

    fun updateAccountInfo() {
        viewModelScope.launch {
            if (isSignedIn) {

                when (val result = dropboxService.api.getCurrentAccount()) {
                    is GetCurrentAccountResult.Error -> {
                        settingsRepository.setAccountDisplayName("")
                        settingsRepository.setAccountEmail("")
                    }

                    is GetCurrentAccountResult.Success -> {
                        settingsRepository.setAccountDisplayName(result.account.name.displayName)
                        settingsRepository.setAccountEmail(result.account.email)
                    }
                }
            } else {
                settingsRepository.setAccountDisplayName("")
                settingsRepository.setAccountEmail("")
            }
        }
    }

    fun updateTodoPath(todoPath: String) {
        viewModelScope.launch {
            settingsRepository.setTodoPath(todoPath)
        }
    }

    fun updateThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            settingsRepository.setThemeMode(mode)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val savedStateHandle = createSavedStateHandle()

                val app = (this[APPLICATION_KEY] as TodotxtAndroidApplication)
                val dropboxService = app.dropboxService
                val settingsRepository = app.settingsRepository

                SettingsViewModel(
                    settingsRepository = settingsRepository,
                    dropboxService = dropboxService,
                    savedStateHandle = savedStateHandle
                )
            }
        }
    }
}