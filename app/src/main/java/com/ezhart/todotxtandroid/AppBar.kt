package com.ezhart.todotxtandroid

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@Composable
fun AppBar(showFilters: () -> Unit, showSettings: () -> Unit) {
    BottomAppBar(
        modifier = Modifier.heightIn(max = 96.dp),
        actions = {

            Column {
                IconButton(onClick = { showFilters() }) {
                    Icon(Icons.Filled.Menu, contentDescription = "Filters")
                }
            }
            Column(modifier = Modifier.weight(1.0f, true)) {}
            Column {
                Row {
                    IconButton(onClick = { /* navigate to search screen */ }
                    ) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }

                    IconButton(onClick = { showSettings() }
                    ) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Settings")
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
    TodotxtAndroidTheme {
        AppBar({}, showSettings = {})
    }
}


