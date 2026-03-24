package com.ezhart.todotxtandroid.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.styleFor
import com.ezhart.todotxtandroid.ui.theme.AppTheme
import java.time.LocalDate

@Composable
fun DetailsCard(
    task: Task,
    modifier: Modifier = Modifier,
    onEditRequest: () -> Unit = {},
    onToggleCompleted: () -> Unit = {},
) {
    Card(modifier = modifier) {
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

                ShareButton(task.task, LocalContext.current)
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

@Composable
fun ShareButton(task:String, context: Context){
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, task)
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, null)

    IconButton(onClick = {context.startActivity(shareIntent, null)}) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share"
        )
    }
}

@Preview(name = "Details Card Light")
@Preview("Details Card Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DetailsCardPreview() {
    AppTheme {
        Surface {
            DetailsCard(
                Task("2025-06-04 Buy apples @shopping +pie due:2025-06-06")
            )
        }
    }
}