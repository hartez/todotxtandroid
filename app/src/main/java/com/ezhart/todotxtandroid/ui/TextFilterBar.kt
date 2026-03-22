package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.ezhart.todotxtandroid.ui.theme.AppTheme

@Composable
fun TextFilterBar(
    filterTextState: TextFieldState,
    onDismiss: () -> Unit
) {

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        actions = {

            IconButton(onClick = {
                filterTextState.clearText()
                onDismiss()
            }
            ) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }

            // TODO Leading icon?

            TextField(
                state = filterTextState,
                placeholder = {
                    Text(
                        "Search...", color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),

                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = { filterTextState.clearText() }
            ) {
                Icon(Icons.Outlined.Clear, contentDescription = "Clear")
            }
        }
    )
}

@Preview(name = "FilterBar Light", showBackground = true)
@Preview("FilterBar Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun SearchBarPreviewText() {
    AppTheme {
        TextFilterBar(TextFieldState()) {}
    }
}