package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@Composable
fun TagsDialog(
    onDismissRequest: () -> Unit,
    options: Map<String, Boolean>,
    onSubmit: (Map<String, Boolean>) -> Unit
) {
    // TODO you found this next bit on a SO post; learn what it does (what's the * operator here?)
    val selections =
        remember { mutableStateMapOf(*options.map { (k, v) -> k to v }.toTypedArray()) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.fillMaxSize()
    ) {

        Card(
            modifier = Modifier
                .fillMaxSize(0.90f)
        ) {
            Column {

                Row(
                    modifier = Modifier
                        .background(color = MaterialTheme.colorScheme.surface)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "Select Projects & Contexts",
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier
                            .padding(16.dp, 8.dp)
                    )
                }

                Row(
                    modifier = Modifier
                        .weight(1f)
                        .background(color = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {

                        for (selection in selections) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable(onClick = {
                                        selections[selection.key] = !selection.value
                                    })
                                    .background(
                                        color =
                                            if (selection.value) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.surface
                                            }
                                    )
                            ) {
                                Text(
                                    text = selection.key,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier
                                        .padding(16.dp, 8.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(color = MaterialTheme.colorScheme.surface),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.Bottom
                ) {
                    TextButton(
                        onClick = { onDismissRequest() },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    TextButton(
                        onClick = {
                            onSubmit(selections)
                            onDismissRequest()
                        },
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }

            }
        }
    }
}

@Preview(name = "Tags Dialog Light")
@Preview("Tags Dialog Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun TagsDialogPreview() {

    val options = mapOf(
        "+paintHouse" to true,
        "+renewLicense" to false,
        "@home" to true,
        "@shopping" to false
    )

    TodotxtAndroidTheme {
        Surface {
            TagsDialog(
                {},
                options,
                {}
            )
        }
    }
}