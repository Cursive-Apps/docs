package com.cursorapps.docs

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.cursorapps.docs.editor.DocumentEditor

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport {
        DocumentEditor()
    }
}
