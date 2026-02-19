package com.ezhart.todotxtandroid

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ezhart.todotxtandroid.ui.theme.TodotxtAndroidTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltersSheet(open:Boolean, onClose: () -> Unit) {
    val sheetState = rememberModalBottomSheetState()
    val coroutineScope = rememberCoroutineScope()

    if (open) {
        ModalBottomSheet(
            onDismissRequest = { onClose() },
            sheetState = sheetState
        ) {
            // Sheet content
            Column(
                modifier = Modifier.Companion
                    .fillMaxWidth()
                    .padding(16.dp)
                    /* TODO make the rounding a setting in dimen.xml or wherever, apply it
                        to the other sheet */
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                Text(text = "This is the filters sheet")
                Button(onClick = {
                    coroutineScope.launch { sheetState.hide() }
                        .invokeOnCompletion {
                            onClose()
                        }
                }) {
                    Text("Close Filter Sheet")
                }
            }
        }
    }
}

@Preview(name = "Filter Sheet Light", showBackground = true)
@Preview("Filter Sheet Dark", uiMode = UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun FilterSheetPreview() {
    TodotxtAndroidTheme {
        FiltersSheet(true, {})
    }
}
