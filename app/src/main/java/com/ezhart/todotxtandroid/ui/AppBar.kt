package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ezhart.todotxtandroid.ui.theme.AppTheme

@Composable
fun AppBar(showFilters: () -> Unit, showSettings: () -> Unit, showSearch: () -> Unit) {
    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        actions = {

            Column {
                IconButton(onClick = { showFilters() }) {
                    Icon(
                        Icons.Outlined.Menu,
                        contentDescription = "Filters"
                    )
                }
            }
            Column(modifier = Modifier.weight(1.0f, true)) {}
            Column {
                Row {
                    IconButton(onClick = { showSearch() }
                    ) {
                        Icon(
                            Icons.Outlined.Search, contentDescription = "Search"
                        )
                    }

                    IconButton(onClick = { showSettings() }
                    ) {
                        Icon(
                            Icons.Outlined.MoreVert, contentDescription = "Settings"
                        )
                    }
                }
            }
        }
    )
}

@Preview(name = "AppBar Light", showBackground = true)
@Preview("AppBar Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun AppBarPreview() {
    AppTheme {
        AppBar({}, showSettings = {}, {})
    }
}


