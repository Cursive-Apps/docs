@file:OptIn(ExperimentalFoundationApi::class)

package com.cursorapps.docs.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max

private val RulerHeight = 24.dp
private val VerticalRulerWidth = 20.dp
private val PageTopGap = 12.dp
private val PageGutter = 24.dp
private val CollapsedOutlineInset = 80.dp

private val DocsColorScheme = lightColorScheme(
    primary = DocsColors.SelectedContent,
    surface = DocsColors.Page,
    surfaceContainer = DocsColors.Page,
    background = DocsColors.Background,
    onSurface = DocsColors.TextPrimary,
)

/** The full word-processor screen used on desktop and web. */
@Composable
fun DocumentEditor(state: EditorState = remember { EditorState() }) {
    val focusRequester = remember { FocusRequester() }
    val actions = remember(state) { EditorActions(state, focusRequester) }

    MaterialTheme(colorScheme = DocsColorScheme) {
        Column(
            Modifier
                .fillMaxSize()
                .background(DocsColors.Background)
                .onPreviewKeyEvent(actions::handleShortcut)
        ) {
            if (state.showMenus) TopBar(state, actions)
            FormattingToolbar(state, actions, Modifier.padding(horizontal = 16.dp, vertical = 6.dp))
            EditorBody(state, actions, focusRequester)
        }
    }
}

@Composable
private fun EditorBody(state: EditorState, actions: EditorActions, focusRequester: FocusRequester) {
    val vScroll = rememberScrollState()
    val hScroll = rememberScrollState()
    val density = LocalDensity.current
    val zoom = state.zoom
    val pageWidth = PageWidth * zoom
    var pageHeight by remember { mutableStateOf(PageHeight * zoom) }

    Box(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxSize()) {
            VerticalRuler(
                pageTop = PageTopGap - with(density) { vScroll.value.toDp() },
                pageHeight = pageHeight,
                margin = PageMargin * zoom,
                inch = PageInch * zoom,
                modifier = Modifier
                    .width(VerticalRulerWidth)
                    .fillMaxHeight()
                    .padding(top = RulerHeight),
            )
            BoxWithConstraints(Modifier.weight(1f).fillMaxHeight()) {
                // Center the page, but never let it slide under the tabs sidebar.
                val leftInset = if (state.showOutline) OutlineWidth else CollapsedOutlineInset
                val pageStart = max((maxWidth - pageWidth) / 2, leftInset)
                val contentWidth = max(maxWidth, pageStart + pageWidth + PageGutter)

                Column(Modifier.fillMaxSize().horizontalScroll(hScroll)) {
                    Box(Modifier.width(contentWidth).height(RulerHeight)) {
                        HorizontalRuler(
                            pageWidth = pageWidth,
                            margin = PageMargin * zoom,
                            inch = PageInch * zoom,
                            modifier = Modifier.padding(start = pageStart).width(pageWidth).fillMaxHeight(),
                        )
                    }
                    Box(
                        Modifier
                            .width(contentWidth)
                            .weight(1f)
                            .verticalScroll(vScroll)
                            .padding(top = PageTopGap, bottom = PageGutter)
                    ) {
                        DocumentPage(
                            state = state,
                            actions = actions,
                            focusRequester = focusRequester,
                            modifier = Modifier
                                .padding(start = pageStart)
                                .onSizeChanged { pageHeight = with(density) { it.height.toDp() } },
                        )
                    }
                }

                val sidebarModifier = Modifier.padding(top = RulerHeight)
                if (state.showOutline) {
                    DocumentOutline(state, sidebarModifier.background(DocsColors.Background))
                } else {
                    ShowOutlineButton(state, sidebarModifier)
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(top = RulerHeight),
            color = DocsColors.PageBorder,
        )
    }
}

/** Editor commands shared by the toolbar, menus and keyboard shortcuts. */
internal class EditorActions(
    private val state: EditorState,
    private val focusRequester: FocusRequester,
) {
    private val text get() = state.selectedTab.text

    /** Runs [block] if the document is editable, then returns focus to the page. */
    fun perform(block: () -> Unit) {
        if (state.mode != EditMode.Viewing) block()
        focusEditor()
    }

    fun format(transform: TextFormat.() -> TextFormat) = perform { state.format = state.format.transform() }
    fun toggleList(kind: ListKind) = perform { text.toggleList(kind) }
    fun indent() = perform { text.indentLines() }
    fun outdent() = perform { text.outdentLines() }
    fun undo() = perform { text.undoState.undo() }
    fun redo() = perform { text.undoState.redo() }

    fun focusEditor() {
        // Throws if the page isn't composed yet (e.g. during the first frame).
        runCatching { focusRequester.requestFocus() }
    }

    fun handleShortcut(event: KeyEvent): Boolean {
        if (event.type != KeyEventType.KeyDown || !(event.isCtrlPressed || event.isMetaPressed)) return false
        val shift = event.isShiftPressed
        when {
            !shift && event.key == Key.B -> format { copy(bold = !bold) }
            !shift && event.key == Key.I -> format { copy(italic = !italic) }
            !shift && event.key == Key.U -> format { copy(underline = !underline) }
            !shift && event.key == Key.Backslash -> perform { state.clearFormatting() }
            !shift && event.key == Key.LeftBracket -> outdent()
            !shift && event.key == Key.RightBracket -> indent()
            shift && event.key == Key.Seven -> toggleList(ListKind.Numbered)
            shift && event.key == Key.Eight -> toggleList(ListKind.Bulleted)
            shift && event.key == Key.Nine -> toggleList(ListKind.Checklist)
            shift && event.key == Key.F -> state.showMenus = !state.showMenus
            else -> return false
        }
        return true
    }
}
