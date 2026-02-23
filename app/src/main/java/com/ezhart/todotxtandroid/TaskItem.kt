package com.ezhart.todotxtandroid

import android.content.res.Configuration.UI_MODE_NIGHT_YES
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import java.time.LocalDate

@Composable
fun TaskItem(
    task: Task,
    onSelect: (Task) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 16.dp)
    ) {

        Column(Modifier.weight(1f)) {
            Text(
                text = displayPriority(task.priority),
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Column(Modifier.weight(15f)) {

            val dueDate = task.dueDate
            val maxLines = when (dueDate) {
                null -> 2
                else -> 1
            }

            Text(
                text = highlightProjectsAndContexts(task),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = maxLines,
                overflow = TextOverflow.Ellipsis
            )

            // TODO Completed tasks should be displaying their completed date
            // (and the due date remains, but in parentheses after the completed date)
            if (dueDate != null) {
                Text(
                    text = formatDueDate(dueDate),
                    style = MaterialTheme.typography.labelMedium,
                )
            }

        }
        Column(Modifier.weight(1f)) {}
    }
}

fun displayPriority(priority: Char?): String {
    return when (priority) {
        null -> " "
        else -> "$priority"
    }
}

fun highlightProjectsAndContexts(task: Task): AnnotatedString {

    val body = task.body
    val all = task.projects.union(task.contexts)

    val highlightRanges = all.map { s ->
        Range(
            SpanStyle(fontWeight = FontWeight.Bold),
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

    val formatRanges = highlightRanges.toMutableList()
    formatRanges.add(
        Range(
            SpanStyle(textDecoration = TextDecoration.LineThrough),
            0, body.length
        )
    )

    return AnnotatedString(
        text = body,
        spanStyles = formatRanges
    )
}


fun formatDueDate(dueDate: LocalDate): AnnotatedString {

    // TODO See if there's something like moment.js we can grab to format the due dates nicer
    // e.g. "Tomorrow", "Monday", etc.

    return buildAnnotatedString {

        if (LocalDate.now() > dueDate) {
            pushStyle(SpanStyle(color = Color.Red))
        }

        append(dueDate.toString())
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