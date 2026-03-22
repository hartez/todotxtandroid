package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.styleFor
import com.ezhart.todotxtandroid.ui.theme.AppTheme
import java.time.LocalDate

// TODO Swipe actions (up to edit, left/right to move to next/prev, down to dismiss dialog)

@Composable
fun DetailsDialog(
    onDismissRequest: () -> Unit, // Don't need this yet, but we will when we implement swipe actions
    task: Task,
    onEditRequest: () -> Unit,
    onToggleCompleted: () -> Unit
) {

    //gist.github.com/fvilarino/ebb3ba8cd643246671ad5ea9b5476d8c

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize(0.90f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row {
                    Text(
                        text = task.taskPriority.display("No Priority"),
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium.styleFor(task)
                    )

                    Text(
                        text =
                            when (task.dueDate) {
                                null -> "No due date"
                                else -> "Due ${task.dueDate}"
                            },
                        color = when (task.dueDate != null && task.dueDate < LocalDate.now()) {
                            true -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        },
                        style = MaterialTheme.typography.bodyMedium.styleFor(task)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 16.dp)
                ) {
                    Text(
                        text = task.body,
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodyLarge.styleFor(task)
                    )
                }

                Row {
                    TextButton(onClick = { onToggleCompleted() }, modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (task.completed) {
                                "MARK PENDING"
                            } else {
                                "MARK COMPLETED"
                            },
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    IconButton(onClick = { onEditRequest() }) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit"
                        )
                    }

                    // TODO implement sharing
                    IconButton(onClick = {}) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share"
                        )
                    }
                }

                Row {
                    Text(
                        text = "Created ${task.createdDate.toString()}",
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


@Preview(name = "Details Dialog Light")
@Preview("Details Dialog Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DetailsDialogPreview() {
    AppTheme {
        Surface {
            DetailsDialog(
                { },
                Task("2025-06-04 Buy apples @shopping +pie due:2025-06-06"),
                {}, onToggleCompleted = {}
            )
        }
    }
}
