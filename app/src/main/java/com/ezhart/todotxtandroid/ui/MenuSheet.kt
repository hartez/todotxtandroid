package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ezhart.todotxtandroid.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuSheet(
    open: Boolean,
    onClose: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onRefresh: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            sheetState = sheetState
        ) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {

                MenuOption(
                    "Refresh",
                    Icons.Outlined.Refresh,
                    false
                ) {
                    onClose()
                    onRefresh()
                }

                MenuOption(
                    "Settings",
                    Icons.Outlined.Settings,
                    false
                ) {
                    onClose()
                    onNavigateToSettings()
                }

            }
        }
    }
}

@Preview(name = "Menu Sheet Light")
@Preview("Menu Sheet Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun MenuSheetPreview() {
    AppTheme {
        Surface {
            MenuSheet(
                true,
                { },
                { }, onRefresh = {})
        }
    }
}