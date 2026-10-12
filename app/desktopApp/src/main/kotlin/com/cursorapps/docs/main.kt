package com.cursorapps.docs

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.cursorapps.docs.editor.DocumentEditor

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Docs",
        state = rememberWindowState(width = 1440.dp, height = 900.dp),
    ) {
        DocumentEditor()
    }
}
