package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.PostAdd
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.ezhart.todotxtandroid.data.Task
import com.ezhart.todotxtandroid.ui.theme.AppTheme
import com.ezhart.todotxtandroid.viewmodels.TaskEditorMode
import com.ezhart.todotxtandroid.viewmodels.TaskEditorUIState
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskEditor(
    editorState: TaskEditorUIState,
    onClose: () -> Unit,
    onSubmit: (markComplete: Boolean) -> Unit,
    listTagsSelections: (String) -> Map<String, Boolean>
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    var isPriorityDialogOpen by remember { mutableStateOf(false) }
    var isTagDialogOpen by remember { mutableStateOf(false) }

    if (editorState.isOpen) {

        val containerColor = MaterialTheme.colorScheme.primaryContainer
        val textEditorState = editorState.textEditorState

        ModalBottomSheet(
            onDismissRequest = { onClose() },
            sheetState = sheetState,
            dragHandle = {},
            containerColor = containerColor
        ) {

            Column(modifier = Modifier.padding(top = 16.dp)) {

                Row {
                    TextField(
                        state = textEditorState,
                        placeholder = {
                            Text(
                                "enter task", color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        lineLimits = TextFieldLineLimits.MultiLine(minHeightInLines = 2),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = containerColor,
                            unfocusedContainerColor = containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),

                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = { onSubmit(false) },
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PostAdd, contentDescription = "Create"
                        )
                    }
                }

                HorizontalDivider()

                Row {

                    IconButton(
                        onClick = { isPriorityDialogOpen = true }) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Priority"
                        )
                    }

                    IconButton(
                        onClick = { isTagDialogOpen = true },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Label,
                            contentDescription = "Projects/Contexts"
                        )
                    }

                    IconButton(
                        onClick = {
                            textEditorState.setTextAndPlaceCursorAtEnd(
                                Task.editDueDate(
                                    textEditorState.text.toString(),
                                    LocalDate.now()
                                )
                            )
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Today, contentDescription = "Due"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    if (editorState.mode == TaskEditorMode.Create) {
                        IconButton(
                            onClick = { onSubmit(true) }) {
                            Icon(
                                imageVector = Icons.Outlined.Done, contentDescription = "Done"
                            )
                        }
                    }

                    if (isPriorityDialogOpen) {
                        Dialog(onDismissRequest = { isPriorityDialogOpen = false }) {
                            PriorityDialog(
                                Task.parsePriority(textEditorState.text.toString()),
                                onPrioritySelected = { taskPriority ->
                                    textEditorState.setTextAndPlaceCursorAtEnd(
                                        Task.editPriority(
                                            textEditorState.text.toString(),
                                            taskPriority
                                        )
                                    )

                                    isPriorityDialogOpen = false
                                }
                            )
                        }
                    }

                    if (isTagDialogOpen) {
                        Dialog(onDismissRequest = { isTagDialogOpen = false }) {
                            TagsDialog(
                                onDismissRequest = { isTagDialogOpen = false },
                                options = listTagsSelections(textEditorState.text.toString()),
                                onSubmit = {
                                    textEditorState.setTextAndPlaceCursorAtEnd(
                                        Task.editTags(
                                            textEditorState.text.toString(),
                                            it.filter { selection -> selection.value }
                                                .map { selection -> selection.key })
                                    )
                                    isPriorityDialogOpen = false
                                }
                            )
                        }
                    }

                }
            }
        }
    }
}


@Preview(name = "New Task Light")
@Preview("New Task Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun NewTaskPreview() {

    val state = TaskEditorUIState(
        true,
        TaskEditorMode.Create,
        TextFieldState()
    )

    AppTheme {
        Surface {
            TaskEditor(
                state, {}, {}, { mapOf() }
            )
        }
    }
}

@Preview(name = "Edit Task Light")
@Preview("Edit Task Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun EditTaskPreview() {

    val state = TaskEditorUIState(
        true,
        TaskEditorMode.Edit,
        TextFieldState()
    ) // TODO Set up task text for this to display

    AppTheme {
        Surface {
            TaskEditor(
                state, {}, {}, { mapOf() }
            )
        }
    }
}