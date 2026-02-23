package com.ezhart.todotxtandroid

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.Dimensions
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import java.time.LocalDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskToggle: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val maxHeightPx = with(LocalDensity.current) { Dimensions.TaskListHeaderExpanded.toPx() }
    val minHeightPx = with(LocalDensity.current) { Dimensions.TaskListHeaderCompact.toPx() }

    val headerHeightPx = remember { mutableFloatStateOf(maxHeightPx) }

    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                val delta = available.y

                if (delta > 0 && headerHeightPx.floatValue >= maxHeightPx) {
                    // Scrolling down and the header's as big as it gets, nothing to do
                    return Offset(0f, 0f)
                }

                if (delta < 0 && headerHeightPx.floatValue <= minHeightPx) {
                    // Scrolling up and the header's as small as it gets, nothing to do
                    return Offset(0f, 0f)
                }

                headerHeightPx.floatValue =
                    (headerHeightPx.floatValue + delta).coerceIn(minHeightPx, maxHeightPx)

                return available;
            }
        }
    }

    val headerHeight = with(LocalDensity.current) { (headerHeightPx.floatValue).toDp() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection)
    ) {

        stickyHeader { Header("All Tasks", tasks.count(), headerHeight) }

        itemsIndexed(tasks) { index, task ->
            TaskItem(
                task = task,
                onToggle = { onTaskToggle(it) }
            )
            if (index < tasks.lastIndex)
                HorizontalDivider(Modifier, thickness = 1.dp, color = Color.Gray)
        }
    }
}

@Composable
fun Header(filter: String, taskCount: Int, height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(height)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.background)
    ) {

        val scale = height / Dimensions.TaskListHeaderExpanded

        Column (
            modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .wrapContentHeight(align = Alignment.CenterVertically)
        ){

            var fontSize = (scale * MaterialTheme.typography.headlineLarge.fontSize)
            if(fontSize.value < MaterialTheme.typography.headlineSmall.fontSize.value){
                    fontSize = MaterialTheme.typography.headlineSmall.fontSize
            }

            Text(
                text = filter,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Left,
                fontSize = fontSize
            )

            Text(
                text = when(taskCount) { 1 -> "1 task" else -> "$taskCount tasks" },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.offset(0.dp, Dimensions.TaskListHeaderExpanded - height)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HeaderLargePreview() {
    Header("All Tasks", 103, Dimensions.TaskListHeaderExpanded)
}

@Preview(showBackground = true)
@Composable
fun HeaderCompactPreview() {
    Header("All Tasks", 103, Dimensions.TaskListHeaderCompact)
}

@Composable
fun TaskItem(
    task: Task,
    onToggle: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(0.dp, 16.dp),

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

    return AnnotatedString(text = body, spanStyles = highlightRanges)
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

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {

    val previewTasks = listOf(
        Task("(A) Thank Mom for the meatballs @phone"),
        Task("(B) Schedule Goodwill pickup +GarageSale @phone"),
        Task("Pay credit card bill due:2025-09-06"),
        Task("Post signs around the neighborhood +GarageSale"),
        Task("@GroceryStore pies"),
        Task("2011-03-02 Document +TodoTxt task format due:2029-08-05"),
        Task("(A) 2011-03-02 Call Mom"),
        Task("(D) A long task that will probably extend more than 2 lines so it will have to be truncated so we can see what that looks like"),
    )

    TodotxtAndroidTheme {
        TaskList(previewTasks, { t -> Unit })
    }
}

