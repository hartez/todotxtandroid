package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.AddTask
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
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.TAG
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskCreatorSheet(
    open: Boolean,
    onClose: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    val containerColor = MaterialTheme.colorScheme.primaryContainer

    val taskText = remember { mutableStateOf("") }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            sheetState = sheetState,
            dragHandle = {},
            containerColor = containerColor
        ) {

            Column(modifier = Modifier.padding(top = 16.dp)) {

                Row {
                    TextField(
                        value = taskText.value,
                        onValueChange = { taskText.value = it },
                        placeholder = {Text("enter task")},
                        minLines = 2,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = containerColor,
                            unfocusedContainerColor = containerColor,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),

                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = {},
                        modifier = Modifier.align(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PostAdd,
                            contentDescription = "Create"
                        )
                    }
                }

                HorizontalDivider()

                Row {

                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Flag,
                            contentDescription = "Priority"
                        )
                    }

                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Label,
                            contentDescription = "Project/Context"
                        )
                    }

                    IconButton(
                        onClick = {},
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Today,
                            contentDescription = "Due"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {}
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Done,
                            contentDescription = "Done"
                        )
                    }

                }
            }
        }


    }

}


@Preview(name = "Task Item Light")
@Preview("Task Item Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TaskCreatorPreview() {
    TodotxtAndroidTheme {
        Surface {
            TaskCreatorSheet(true, {})
        }
    }
}