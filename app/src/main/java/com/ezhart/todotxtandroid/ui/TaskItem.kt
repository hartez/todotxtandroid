package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.styleFor
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import java.time.LocalDate

@Composable
fun TaskItem(
    task: Task,
    onSelect: (Task) -> Unit
) {
    Row(

        modifier = Modifier
            .clickable(onClick = { onSelect(task) })
            .fillMaxWidth()
            .padding(0.dp, 16.dp)
    ) {

        Column(Modifier.weight(1f)) {
            Text(
                text = task.taskPriority.display(),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(Modifier.weight(15f)) {

            val displayDateLine = (task.dueDate != null || task.completed)

            val maxLines = when (displayDateLine) {
                false -> 2
                else -> 1
            }

            Text(
                text = highlightProjectsAndContexts(
                    task,
                    MaterialTheme.colorScheme.onSurfaceVariant
                ),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium.styleFor(task),
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )

            if (displayDateLine) {
                Text(
                    text = formatDateLine(task, MaterialTheme.colorScheme.error),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

        }
        Column(Modifier.weight(1f)) {}
    }
}

fun highlightProjectsAndContexts(task: Task, color: Color): AnnotatedString {

    val body = task.body
    val all = task.projects.union(task.contexts)

    val highlightRanges = all.map { s ->
        Range(
            SpanStyle(color = color),
            body.indexOf(s),
            body.indexOf(s) + s.length
        )
    }

    if (!task.completed) {
        return AnnotatedString(
            text = body,
            spanStyles = highlightRanges
        )
    }

    return AnnotatedString(
        text = body,
        spanStyles = highlightRanges
    )
}


fun formatDateLine(task: Task, overdueColor: Color): AnnotatedString {

    // TODO See if there's something like moment.js we can grab to format the due dates nicer
    // e.g. "Tomorrow", "Monday", etc.

    return buildAnnotatedString {

        if (task.completed) {
            append("Completed " + task.completedDate.toString())
        }

        val dueDate = task.dueDate

        if (dueDate != null) {

            if (task.completed) {
                append(" (due $dueDate)")
            } else if (LocalDate.now() > dueDate) {
                pushStyle(SpanStyle(color = overdueColor))
                append(dueDate.toString())
            } else {
                append(dueDate.toString())
            }
        }
    }
}

@Preview(name = "Task Item Light")
@Preview("Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TaskItemPreview() {
    TodotxtAndroidTheme {
        Surface {
            TaskItem(Task("(B) Schedule Goodwill pickup +GarageSale @phone")) {}
        }
    }
}

@Preview(name = "Completed Task Item Light")
@Preview("Completed Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CompletedTaskItemPreview() {
    TodotxtAndroidTheme {
        Surface {
            TaskItem(Task("x 2026-01-01 Schedule Goodwill pickup +GarageSale @phone")) {}
        }
    }
}

@Preview(name = "Due Task Item Light")
@Preview("Due Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun DueTaskItemPreview() {

    val due = LocalDate.now().plusDays(5)

    TodotxtAndroidTheme {
        Surface {
            TaskItem(Task("2026-01-01 Schedule Goodwill pickup due:$due +GarageSale @phone")) {}
        }
    }
}

@Preview(name = "Overdue Task Item Light")
@Preview("Overdue Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun OverdueTaskItemPreview() {

    val due = LocalDate.now().minusDays(5)

    TodotxtAndroidTheme {
        Surface {
            TaskItem(Task("2026-01-01 Schedule Goodwill pickup due:$due +GarageSale @phone")) {}
        }
    }
}

@Preview(name = "Completed Overdue Task Item Light")
@Preview("Completed Overdue Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CompletedOverdueTaskItemPreview() {

    val due = LocalDate.now().minusDays(5)
    val completed = LocalDate.now()

    TodotxtAndroidTheme {
        Surface {
            TaskItem(Task("x $completed 2026-01-01 Schedule Goodwill pickup due:$due +GarageSale @phone")) {}
        }
    }
}