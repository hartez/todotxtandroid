package com.ezhart.todotxtandroid

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.data.CompletedFilter
import com.ezhart.todotxtandroid.data.DueFilter
import com.ezhart.todotxtandroid.data.PendingFilter
import com.ezhart.todotxtandroid.ui.theme.Dimensions
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(
    open: Boolean, onClose: () -> Unit,
    onUpdateFilter: (Any) -> Unit, selectedFilter: Any
) {

    val sheetState = rememberModalBottomSheetState()

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            sheetState = sheetState
        ) {
            // Sheet content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(
                        RoundedCornerShape(
                            topStart = Dimensions.SheetCornerRadius,
                            topEnd = Dimensions.SheetCornerRadius
                        )
                    )
                    .padding(0.dp, 16.dp, 0.dp, 0.dp)
            ) {

                FilterOption("All Tasks", Icons.Outlined.Inbox, selectedFilter is AllTasksFilter) {
                    onUpdateFilter(AllTasksFilter)
                    onClose()
                }

                FilterOption("Due", Icons.Outlined.Timer, selectedFilter is DueFilter) {
                    onUpdateFilter(DueFilter)
                    onClose()
                }

                FilterOption(
                    "Pending",
                    Icons.Outlined.CheckBoxOutlineBlank,
                    selectedFilter is PendingFilter
                ) {
                    onUpdateFilter(PendingFilter)
                    onClose()
                }

                FilterOption("Completed", Icons.Outlined.Check, selectedFilter is CompletedFilter) {
                    onUpdateFilter(CompletedFilter)
                    onClose()
                }

                HorizontalDivider()

                Text("Gonna put the project/context filters here")
            }
        }
    }
}

@Composable
fun FilterOption(text: String, icon: ImageVector, selected: Boolean, onSelected: () -> Unit) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                when (selected) {
                    true -> MaterialTheme.colorScheme.primaryContainer // TODO Nicer color for this (need a appropriate one for dark mode, too)
                    else -> Color.Transparent
                }
            )

            .padding(8.dp, 8.dp)
            .clickable {
                if (!selected) {
                    onSelected()
                }
            }
    )
    {
        Icon(imageVector = icon, contentDescription = text, tint = MaterialTheme.colorScheme.onPrimaryContainer)
        Spacer(Modifier.width(16.dp))
        Text(text = text, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }

}

@Preview(name = "Filter Sheet Light")
@Preview("Filter Sheet Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FilterSheetPreview() {
    TodotxtAndroidTheme {
        Surface {
            FiltersSheet(true, {}, selectedFilter = AllTasksFilter, onUpdateFilter = {})
        }
    }
}

@Preview(name = "Filter Option Light")
@Preview(name = "Filter Option Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FilterOptionPreview() {
    TodotxtAndroidTheme {
        Surface {
            Column{
                FilterOption(
                    "All Tasks", Icons.Outlined.Inbox, true,
                    { })

                FilterOption(
                    "All Tasks", Icons.Outlined.Inbox, false,
                    { })
            }

        }
    }
}
