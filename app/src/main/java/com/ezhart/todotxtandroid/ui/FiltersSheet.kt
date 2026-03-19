package com.ezhart.todotxtandroid.ui

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckBoxOutlineBlank
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.data.AllTasksFilter
import com.ezhart.todotxtandroid.data.CompletedFilter
import com.ezhart.todotxtandroid.data.ContextFilter
import com.ezhart.todotxtandroid.data.DueFilter
import com.ezhart.todotxtandroid.data.Filter
import com.ezhart.todotxtandroid.data.PendingFilter
import com.ezhart.todotxtandroid.data.ProjectFilter
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

enum class ExpandedOption {
    None,
    Projects,
    Contexts
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(
    allProjects: List<String>,
    allContexts: List<String>,
    open: Boolean, onClose: () -> Unit,
    onUpdateFilter: (Filter) -> Unit, selectedFilter: Any
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var expandedOption by remember { mutableStateOf(ExpandedOption.None) }

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            sheetState = sheetState
        ) {
            // Sheet content
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {

                MenuOption("All Tasks", Icons.Outlined.Inbox, selectedFilter is AllTasksFilter) {
                    onUpdateFilter(AllTasksFilter)
                    expandedOption = ExpandedOption.None
                    onClose()
                }

                MenuOption("Due", Icons.Outlined.Timer, selectedFilter is DueFilter) {
                    onUpdateFilter(DueFilter)
                    expandedOption = ExpandedOption.None
                    onClose()
                }

                MenuOption(
                    "Pending",
                    Icons.Outlined.CheckBoxOutlineBlank,
                    selectedFilter is PendingFilter
                ) {
                    onUpdateFilter(PendingFilter)
                    expandedOption = ExpandedOption.None
                    onClose()
                }

                MenuOption("Completed", Icons.Outlined.Check, selectedFilter is CompletedFilter) {
                    onUpdateFilter(CompletedFilter)
                    expandedOption = ExpandedOption.None
                    onClose()
                }

                HorizontalDivider()

                ExpandingOption(
                    "Projects",
                    expandedOption == ExpandedOption.Projects,
                    {
                        expandedOption = if (expandedOption == ExpandedOption.Projects) {
                            ExpandedOption.None
                        } else {
                            ExpandedOption.Projects
                        }
                    },
                    allProjects,
                    selectedOption(selectedFilter)
                ) {
                    onUpdateFilter(ProjectFilter(it))
                    onClose()
                }

                ExpandingOption(
                    "Contexts",
                    expandedOption == ExpandedOption.Contexts,
                    {
                        expandedOption = if (expandedOption == ExpandedOption.Contexts) {
                            ExpandedOption.None
                        } else {
                            ExpandedOption.Contexts
                        }
                    },
                    allContexts,
                    selectedOption(selectedFilter)
                ) {
                    onUpdateFilter(ContextFilter(it))
                    onClose()
                }

                Spacer(Modifier.height(8.dp))

            }
        }
    }
}

fun selectedOption(filter: Any): String? {
    return when (filter) {
        is ProjectFilter -> filter.project
        is ContextFilter -> filter.context
        else -> null
    }
}

@Preview(name = "Filter Sheet Light")
@Preview("Filter Sheet Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun FilterSheetPreview() {
    TodotxtAndroidTheme {
        Surface {
            FiltersSheet(
                listOf(),
                listOf(),
                true,
                {},
                selectedFilter = AllTasksFilter,
                onUpdateFilter = {})
        }
    }
}

