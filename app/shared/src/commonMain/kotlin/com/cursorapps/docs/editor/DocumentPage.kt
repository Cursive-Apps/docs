package com.cursorapps.docs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.input.TextFieldDecorator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** US Letter at 96 dpi, with 1" margins. */
internal val PageInch = 96.dp
internal val PageWidth = PageInch * 8.5f
internal val PageHeight = PageInch * 11f
internal val PageMargin = PageInch

private const val POINTS_TO_DP = 96f / 72f

@Composable
internal fun DocumentPage(
    state: EditorState,
    actions: EditorActions,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val tab = state.selectedTab
    val format = tab.format
    val zoom = state.zoom
    val fontSize = format.fontSize * POINTS_TO_DP * zoom

    val textStyle = TextStyle(
        fontFamily = format.font.family,
        fontSize = fontSize.sp,
        lineHeight = (fontSize * 1.15f * format.lineSpacing).sp,
        fontWeight = if (format.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (format.italic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = if (format.underline) TextDecoration.Underline else null,
        color = format.textColor,
        background = format.highlight,
        textAlign = format.align,
    )

    // Put the cursor in the page on launch and whenever the tab changes.
    LaunchedEffect(tab.id) {
        withFrameNanos { }
        actions.focusEditor()
    }

    Box(
        modifier
            .width(PageWidth * zoom)
            .heightIn(min = PageHeight * zoom)
            .background(DocsColors.Page)
            .border(1.dp, DocsColors.PageBorder)
    ) {
        BasicTextField(
            state = tab.text,
            readOnly = state.mode == EditMode.Viewing,
            textStyle = textStyle,
            cursorBrush = SolidColor(DocsColors.TextPrimary),
            // Padding lives in the decorator so clicks in the margins still focus the text.
            decorator = TextFieldDecorator { inner -> Box(Modifier.padding(PageMargin * zoom)) { inner() } },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = PageHeight * zoom)
                .focusRequester(focusRequester),
        )
    }
}
