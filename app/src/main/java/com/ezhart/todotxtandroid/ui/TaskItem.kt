package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.ezhart.todotxtandroid.viewmodels.SwipeOption
import com.ezhart.todotxtandroid.viewmodels.TaskSwipeOptions
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun TaskItem(
    task: Task,
    onSelect: (Task) -> Unit,
    swipeOptions: TaskSwipeOptions
) {

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        SwipeToDismissBoxValue.Settled,
        SwipeToDismissBoxDefaults.positionalThreshold
    )

    val coroutineScope = rememberCoroutineScope()

    SwipeToDismissBox(
        swipeToDismissBoxState,
        enableDismissFromEndToStart = swipeOptions.endToStartOption != null,
        enableDismissFromStartToEnd = swipeOptions.startToEndOption != null,

        // TODO go ahead and add edit (mark selected, then set value to open editor, can do it all in viewmodel)
        // TODO toast message when toggling complete
        // TODO Undo button in toast alert?

        backgroundContent = {
            SwipeActionBackground(swipeToDismissBoxState.dismissDirection, swipeOptions)
        },
        onDismiss = {
            when (it) {
                SwipeToDismissBoxValue.EndToStart -> {
                    coroutineScope.launch {
                        swipeToDismissBoxState.reset()
                        swipeOptions.endToStartOption?.onSwipe(task)
                    }

                }

                else -> {}
            }
        }

    ) {

        Row(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
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

@Composable
fun SwipeActionBackground(
    swipeDirection: SwipeToDismissBoxValue,
    options: TaskSwipeOptions
) {
    when (swipeDirection) {
        SwipeToDismissBoxValue.Settled -> return
        SwipeToDismissBoxValue.StartToEnd -> {
            SwipeActionBackground(
                options.startToEndOption?.backgroundColor ?: Color.LightGray,
                Alignment.CenterStart,
                options.startToEndOption?.foregroundColor
                    ?: Color.LightGray,
                options.startToEndOption?.label ?: "",
                options.startToEndOption?.icon
                    ?: Icons.Outlined.ErrorOutline,
                textFirst = false
            )
        }

        SwipeToDismissBoxValue.EndToStart -> {
            SwipeActionBackground(
                options.endToStartOption?.backgroundColor ?: Color.LightGray,
                Alignment.CenterEnd,
                options.endToStartOption?.foregroundColor
                    ?: Color.LightGray,
                options.endToStartOption?.label ?: "",
                options.endToStartOption?.icon
                    ?: Icons.Outlined.ErrorOutline,
                textFirst = true
            )
        }
    }
}

@Composable
fun SwipeActionBackground(
    background: Color,
    alignment: Alignment,
    foreground: Color,
    label: String,
    icon: ImageVector,
    textFirst: Boolean
) {

    val backgroundColor by animateColorAsState(
        background,
        label = "background color"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {

            if (textFirst) {
                Text(label, color = foreground)
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = foreground
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = foreground
                )
                Text(label, color = foreground)
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
            TaskItem(
                Task("(B) Schedule Goodwill pickup +GarageSale @phone"),
                {},
                TaskSwipeOptions()
            )
        }
    }
}

@Preview(name = "Completed Task Item Light")
@Preview("Completed Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun CompletedTaskItemPreview() {
    TodotxtAndroidTheme {
        Surface {
            TaskItem(
                Task("x 2026-01-01 Schedule Goodwill pickup +GarageSale @phone"),
                {},
                TaskSwipeOptions()
            )
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
            TaskItem(
                Task("2026-01-01 Schedule Goodwill pickup due:$due +GarageSale @phone"),
                {},
                TaskSwipeOptions()
            )
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
            TaskItem(
                Task("2026-01-01 Schedule Goodwill pickup due:$due +GarageSale @phone"),
                {},
                TaskSwipeOptions()
            )
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
            TaskItem(
                Task("x $completed 2026-01-01 Schedule Goodwill pickup due:$due +GarageSale @phone"),
                {},
                TaskSwipeOptions()
            )
        }
    }
}


@Preview(name = "Swipe Light")
@Preview("Swipe Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun SwipeBackgroundPreview() {

    val options = TaskSwipeOptions(
        endToStartOption = SwipeOption(
            label = "MARK COMPLETE",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            icon = Icons.Outlined.Check,
            {}
        ),
        startToEndOption = SwipeOption(
            label = "EDIT",
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            icon = Icons.Outlined.Edit,
            {}
        ))

    TodotxtAndroidTheme {
        Surface {
            Column {
                Row(modifier = Modifier.height(75.dp)) {
                    SwipeActionBackground(
                        swipeDirection = SwipeToDismissBoxValue.EndToStart,
                        options
                    )
                }
                Row(modifier = Modifier.height(75.dp)) {
                    SwipeActionBackground(
                        swipeDirection = SwipeToDismissBoxValue.StartToEnd,
                        options
                    )
                }
            }
        }
    }
}
