@file:OptIn(ExperimentalFoundationApi::class)

package com.cursorapps.docs.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun TopBar(state: EditorState, actions: EditorActions) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        DocsLogo(Modifier.size(width = 30.dp, height = 40.dp))
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DocumentTitleField(
                    title = state.title,
                    onTitleChange = { state.title = it },
                )
                ToolbarIconButton(
                    icon = if (state.starred) Icons.Filled.Star else Icons.Outlined.StarOutline,
                    tooltip = if (state.starred) "Unstar" else "Star",
                    onClick = { state.starred = !state.starred },
                    iconSize = 18.dp,
                )
            }
            MenuBar(state, actions)
        }
        Spacer(Modifier.width(8.dp))
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Outlined.Comment,
            tooltip = "Open comment history",
            onClick = {},
            size = 40.dp,
            iconSize = 24.dp,
        )
        Spacer(Modifier.width(12.dp))
        ShareButton()
        Spacer(Modifier.width(12.dp))
        Avatar(Modifier.size(40.dp))
    }
}

@Composable
private fun DocumentTitleField(title: String, onTitleChange: (String) -> Unit) {
    var draft by remember(title) { mutableStateOf(title) }
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    val focused by interaction.collectIsFocusedAsState()
    val focusManager: FocusManager = LocalFocusManager.current

    // Commit when focus leaves the field; an empty title falls back to the default.
    LaunchedEffect(focused) {
        if (!focused && draft != title) onTitleChange(draft.ifBlank { DEFAULT_DOCUMENT_TITLE })
    }

    BasicTextField(
        value = draft,
        onValueChange = { draft = it },
        singleLine = true,
        interactionSource = interaction,
        textStyle = TextStyle(fontSize = 18.sp, color = DocsColors.TextPrimary),
        cursorBrush = SolidColor(DocsColors.TextPrimary),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
        modifier = Modifier.hoverable(interaction),
        decorationBox = { inner ->
            Box(
                Modifier
                    .border(
                        width = if (focused) 2.dp else 1.dp,
                        color = when {
                            focused -> DocsColors.SelectedContent
                            hovered -> DocsColors.TextSecondary
                            else -> Color.Transparent
                        },
                        shape = RoundedCornerShape(4.dp),
                    )
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) { inner() }
        },
    )
}

@Composable
private fun MenuBar(state: EditorState, actions: EditorActions) {
    val tab = state.selectedTab
    val menus: List<Pair<String, @Composable (close: () -> Unit) -> Unit>> = listOf(
        "File" to { close ->
            MenuItem("New tab", { state.addTab(); close() })
            MenuItem("Delete tab", { state.deleteTab(state.selectedTabId); close() }, enabled = state.tabs.size > 1)
        },
        "Edit" to { close ->
            MenuItem("Undo", { tab.text.undoState.undo(); close() }, enabled = tab.text.undoState.canUndo, trailingText = "Ctrl+Z")
            MenuItem("Redo", { tab.text.undoState.redo(); close() }, enabled = tab.text.undoState.canRedo, trailingText = "Ctrl+Y")
        },
        "View" to { close ->
            MenuItem("Show tabs & outlines", { state.showOutline = !state.showOutline; close() }, checked = state.showOutline)
            MenuItem("Show menus", { state.showMenus = !state.showMenus; close() }, checked = state.showMenus)
            HorizontalDivider()
            EditMode.entries.forEach { mode ->
                MenuItem(mode.label, { state.mode = mode; close() }, leadingIcon = mode.icon, checked = state.mode == mode)
            }
        },
        "Insert" to { close ->
            MenuItem("Bulleted list", { close(); actions.toggleList(ListKind.Bulleted) })
            MenuItem("Numbered list", { close(); actions.toggleList(ListKind.Numbered) })
            MenuItem("Checklist", { close(); actions.toggleList(ListKind.Checklist) })
        },
        "Format" to { close ->
            MenuItem("Bold", { state.format = state.format.copy(bold = !state.format.bold); close() }, checked = state.format.bold, trailingText = "Ctrl+B")
            MenuItem("Italic", { state.format = state.format.copy(italic = !state.format.italic); close() }, checked = state.format.italic, trailingText = "Ctrl+I")
            MenuItem("Underline", { state.format = state.format.copy(underline = !state.format.underline); close() }, checked = state.format.underline, trailingText = "Ctrl+U")
            HorizontalDivider()
            MenuItem("Clear formatting", { state.clearFormatting(); close() }, trailingText = "Ctrl+\\")
        },
        "Tools" to { close ->
            MenuItem("Word count", { close() }, trailingText = wordCountLabel(tab.text.text))
            MenuItem("Spelling and grammar", { close() }, enabled = false)
        },
        "Extensions" to { close -> MenuItem("Add-ons", { close() }, enabled = false) },
        "Help" to { close -> MenuItem("Docs help", { close() }, enabled = false) },
    )

    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        menus.forEach { (label, items) ->
            var expanded by remember { mutableStateOf(false) }
            Box {
                ChromeButton(
                    onClick = { expanded = true },
                    selected = expanded,
                    modifier = Modifier.height(24.dp),
                ) {
                    Text(label, style = ToolbarTextStyle, modifier = Modifier.padding(horizontal = 7.dp))
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    containerColor = DocsColors.Page,
                ) {
                    items { expanded = false }
                }
            }
        }
    }
}

private fun wordCountLabel(text: CharSequence): String {
    val words = text.split(Regex("\\s+")).count { it.isNotBlank() }
    return if (words == 1) "1 word" else "$words words"
}

@Composable
private fun ShareButton() {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clip(CircleShape)
            .background(DocsColors.ShareButton),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChromeButton(
            onClick = {},
            tooltip = "Private to only me",
            shape = RoundedCornerShape(topStartPercent = 50, bottomStartPercent = 50),
            modifier = Modifier.height(40.dp),
        ) {
            Row(Modifier.padding(start = 16.dp, end = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Lock, contentDescription = null, tint = DocsColors.ShareButtonContent, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Share", fontSize = 14.sp, color = DocsColors.ShareButtonContent)
            }
        }
        Box(Modifier.width(1.dp).height(40.dp).background(DocsColors.Background))
        ChromeButton(
            onClick = {},
            tooltip = "Quick sharing actions",
            shape = RoundedCornerShape(topEndPercent = 50, bottomEndPercent = 50),
            modifier = Modifier.size(width = 36.dp, height = 40.dp),
        ) {
            Icon(Icons.Filled.ArrowDropDown, contentDescription = "Quick sharing actions", tint = DocsColors.ShareButtonContent)
        }
    }
}

@Composable
private fun Avatar(modifier: Modifier = Modifier) {
    WithTooltip("Account") {
        Canvas(modifier) {
            val radius = size.minDimension / 2
            drawCircle(Color(0xFF1A73E8), radius)
            drawCircle(Color.White, radius - 2.dp.toPx())
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFE0B2), Color(0xFFE040FB), Color(0xFF3949AB), Color(0xFF0D0B2E)),
                    center = Offset(size.width * 0.45f, size.height * 0.4f),
                    radius = radius,
                ),
                radius = radius - 4.dp.toPx(),
            )
        }
    }
}

/** The blue Docs page glyph with a folded corner and text lines. */
@Composable
internal fun DocsLogo(modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val fold = w * 0.32f
        val corner = 2.dp.toPx()
        val body = Path().apply {
            moveTo(corner, 0f)
            lineTo(w - fold, 0f)
            lineTo(w, fold)
            lineTo(w, h - corner)
            quadraticTo(w, h, w - corner, h)
            lineTo(corner, h)
            quadraticTo(0f, h, 0f, h - corner)
            lineTo(0f, corner)
            quadraticTo(0f, 0f, corner, 0f)
            close()
        }
        drawPath(body, DocsColors.LogoBlue)
        val foldPath = Path().apply {
            moveTo(w - fold, 0f)
            lineTo(w - fold, fold)
            lineTo(w, fold)
            close()
        }
        drawPath(foldPath, DocsColors.LogoFold)

        val lineHeight = h * 0.055f
        val left = w * 0.22f
        val lineWidth = w * 0.56f
        listOf(0.48f, 0.60f, 0.72f).forEachIndexed { i, y ->
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, h * y),
                size = Size(if (i == 2) lineWidth * 0.6f else lineWidth, lineHeight),
                cornerRadius = CornerRadius(lineHeight / 2),
            )
        }
    }
}
