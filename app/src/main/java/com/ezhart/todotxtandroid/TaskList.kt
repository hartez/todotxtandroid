package com.ezhart.todotxtandroid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.AnnotatedString.Range
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskToggle: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
fun TaskItem(
    task: Task,
    onToggle: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = highlightProjectsAndContexts(task.body),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        // TODO Add due as a property from the metadata on Task
        // And if it's not null, it should show up in a text field on the
        // second line. If it does, then maxLines above should be 1

        // TODO See if there's something like moment built into Android
        // for converting the due date to a nice string, otherwise grab
        // a library to handle it
    }
}

// TODO Once we've got the projects/contexts loaded up for the tasks,
// we can use that list to find all of the indices/lengths in the body text
// and highlight them.
fun highlightProjectsAndContexts(body: String): AnnotatedString {
    val spanStyles = listOf(
        Range(
            SpanStyle(fontWeight = FontWeight.Bold),
            start = 3,
            end = 5
        )
    )

    return AnnotatedString(text = body, spanStyles = spanStyles)
}

@Preview(showBackground = true)
@Composable
fun TaskListPreview() {

    val previewTasks = listOf<Task>(
        Task("(A) Thank Mom for the meatballs @phone"),
        Task("(B) Schedule Goodwill pickup +GarageSale @phone"),
        Task("Post signs around the neighborhood +GarageSale"),
        Task("@GroceryStore pies"),
        Task("2011-03-02 Document +TodoTxt task format"),
        Task("(A) 2011-03-02 Call Mom"),
        Task("A long task that will probably extend more than 2 lines so it will have to be truncated so we can see what that looks like"),
    )

    TodotxtAndroidTheme {
        TaskList(previewTasks, { t -> Unit })
    }
}
