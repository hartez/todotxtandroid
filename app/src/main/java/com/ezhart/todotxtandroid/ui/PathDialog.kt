package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.ui.theme.AppTheme

@Composable
fun PathDialog(
    onDismissRequest: () -> Unit,
    path: String,
    onConfirmation: (String) -> Unit
) {
    val updated = remember { mutableStateOf(path) }

    Card(
        modifier = Modifier
            .height(375.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ) {

            Row {
                Column (verticalArrangement = Arrangement.spacedBy(16.dp)){
                    Text(
                        text = "Todo.txt file path",
                        style = MaterialTheme.typography.labelLarge
                    )
                    TextField(
                        value = updated.value,
                        { updated.value = it },
                        singleLine = true
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.Bottom
            ) {
                TextButton(
                    onClick = { onDismissRequest() },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Dismiss")
                }
                TextButton(
                    onClick = {
                        onConfirmation(updated.value)
                        onDismissRequest()
                    },
                    modifier = Modifier.padding(8.dp),
                ) {
                    Text("Confirm")
                }
            }
        }
    }
}

@Preview(name = "Path Dialog Light")
@Preview("Path Dialog Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PathDialogPreview() {
    AppTheme {
        Surface {
            PathDialog(
                { },
                "/todo/todo.txt",
                { })
        }
    }
}
