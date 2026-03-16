package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.times
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.Dimensions
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import com.ezhart.todotxtandroid.viewmodels.SwipeOption
import com.ezhart.todotxtandroid.viewmodels.TaskSwipeOptions

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskList(
    tasks: List<Task>,
    header: String,
    onSelect: (Task) -> Unit,
    onToggleCompleted: (Task) -> Unit,
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

                return available
            }
        }
    }

    // TODO Keep on eye on scrollIndicatorState APIs (https://michaelevans.org/blog/2026/02/11/custom-scroll-indicators-in-jetpack-compose-foundation/)
    // so we can add proper scroll indicators to the task list

    val headerHeight = with(LocalDensity.current) { (headerHeightPx.floatValue).toDp() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(connection)
    ) {

        stickyHeader { Header(header, tasks.count(), headerHeight) }

        itemsIndexed(tasks,
            key = { _, t -> t.task }
        ) { index, task ->
            TaskItem(
                task = task,
                onSelect = { onSelect(it) },
                TaskSwipeOptions(
                    endToStartOption = SwipeOption(
                        "Toggle Complete",
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        Icons.Outlined.Check,
                        { onToggleCompleted(it) })
                )
            )
            if (index < tasks.lastIndex)
                HorizontalDivider()
        }
    }
}

@Composable
fun Header(text: String, taskCount: Int, height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(height)
            .clipToBounds()
            .background(MaterialTheme.colorScheme.surface)
    ) {

        val scale = height / Dimensions.TaskListHeaderExpanded
        val padding = max(32.dp * scale, 8.dp)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .wrapContentHeight(align = Alignment.CenterVertically)
        ) {

            var fontSize = (scale * MaterialTheme.typography.headlineLarge.fontSize)
            if (fontSize.value < MaterialTheme.typography.headlineSmall.fontSize.value) {
                fontSize = MaterialTheme.typography.headlineSmall.fontSize
            }

            Text(
                text = text,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Left,
                fontSize = fontSize
            )

            Text(
                text = when (taskCount) {
                    1 -> "1 task"
                    else -> "$taskCount tasks"
                },
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.offset(0.dp, Dimensions.TaskListHeaderExpanded - height)
            )
        }
    }
}

@Preview("Header Large Light")
@Preview("Header Large Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HeaderLargePreview() {
    TodotxtAndroidTheme {
        Surface {
            Header("All Tasks", 103, Dimensions.TaskListHeaderExpanded)
        }
    }
}

@Preview("Header Compact Light")
@Preview("Header Compact Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HeaderCompactPreview() {
    TodotxtAndroidTheme {
        Surface {
            Header("All Tasks", 103, Dimensions.TaskListHeaderCompact)
        }
    }
}

@Preview("Task List Light")
@Preview("Task List Dark", uiMode = UI_MODE_NIGHT_YES)
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
        Surface {
            TaskList(previewTasks, "All Tasks", {}, onToggleCompleted = {})
        }
    }
}

