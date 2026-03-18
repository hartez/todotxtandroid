package com.ezhart.todotxtandroid.viewmodels

import androidx.compose.material3.SnackbarDuration

data class MessageUIState(
    val pending: Boolean = false,
    val text: String = "",
    val onDismiss: () -> Unit = {},
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionLabel: String? = null,
    val action: (() -> Unit)? = null,
)