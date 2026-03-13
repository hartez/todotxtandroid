package com.ezhart.todotxtandroid

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.ezhart.todotxtandroid.data.Task

val Any.TAG: String
    get() {
        return if (!javaClass.isAnonymousClass) {
            javaClass.simpleName
        } else {
            javaClass.name
        }
    }

fun TextStyle.styleFor(task: Task): TextStyle {
    if (task.completed) {
        return this.merge(
            TextStyle(textDecoration = TextDecoration.LineThrough)
        )
    }

    return this
}