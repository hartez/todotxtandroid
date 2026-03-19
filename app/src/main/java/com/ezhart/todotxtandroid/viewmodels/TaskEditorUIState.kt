package com.ezhart.todotxtandroid.viewmodels

import androidx.compose.foundation.text.input.TextFieldState

enum class TaskEditorMode {
    Create,
    Edit
}

data class TaskEditorUIState(
    val isOpen: Boolean,
    val mode: TaskEditorMode,
    val textEditorState: TextFieldState
)


