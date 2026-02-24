package com.ezhart.todotxtandroid

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme

@Composable
fun ExpandingOption(
    text: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    options: List<String>,
    selectedOption: String? = null,
    onSelected: (String) -> Unit
) {
    Column(

    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onToggle()
                }
                .padding(8.dp, 8.dp)
        )
        {
            val expansionIcon = when (expanded) {
                true -> Icons.Outlined.KeyboardArrowUp
                else -> Icons.Outlined.KeyboardArrowDown
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.Label,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.width(16.dp))
            Text(text = text, color = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.weight(1f))
            Icon(
                imageVector = expansionIcon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        if (expanded) {
            LazyColumn(

            ) {
                items(options) { option ->
                    FilterOption(
                        text = option,
                        null,
                        selectedOption == option
                    ) { onSelected(option) }
                }
            }
        }
    }
}

// TODO The bottom of the expanded options falls behind the system controls, need to futz with safe area settings

@Preview(name = "Expanding Option Light")
@Preview("Expanding Option Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExpandingOptionPreview() {
    TodotxtAndroidTheme {
        Surface {
            ExpandingOption(
                "Contexts",
                false, onToggle = {}, listOf(), onSelected = {})
        }
    }
}

@Preview(name = "Expanding Option Open Light")
@Preview("Expanding Option Open Dark", uiMode = UI_MODE_NIGHT_YES)
@Composable
fun ExpandingOptionOpenPreview() {
    TodotxtAndroidTheme {
        Surface {
            ExpandingOption(
                "Projects",
                true, onToggle = {}, listOf("+shopping", "+paint", "+fixDrain"), onSelected = {})
        }
    }
}
