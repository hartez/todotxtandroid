package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.NoPriority
import com.ezhart.todotxtandroid.data.TaskPriority
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@Composable
fun PriorityDialog(
    selectedPriority: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize(0.90f)
        ) {
            Column {

                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Select Priority",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(16.dp, 8.dp) // TODO Put this padding and the tags dialog padding into dimensions
                    )
                }

                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                    for (priority in TaskPriority.options) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(onClick = { onPrioritySelected(priority) })
                                .background(
                                    color =
                                        if (priority == selectedPriority) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surface
                                        }
                                )
                        ) {
                            Text(
                                text = priority.display("None"),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(16.dp, 8.dp)
                            )
                        }
                    }
                }

            }
        }
    }
}

@Preview(name = "Priority Dialog Light")
@Preview("Priority Dialog Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PriorityDialogPreview() {
    TodotxtAndroidTheme {
        Surface {
            PriorityDialog(
                NoPriority,
                {}
            )
        }
    }
}