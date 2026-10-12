@file:OptIn(ExperimentalFoundationApi::class)

package com.cursorapps.docs.editor

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatAlignLeft
import androidx.compose.material.icons.automirrored.outlined.FormatAlignRight
import androidx.compose.material.icons.automirrored.outlined.FormatIndentDecrease
import androidx.compose.material.icons.automirrored.outlined.FormatIndentIncrease
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.automirrored.outlined.Redo
import androidx.compose.material.icons.automirrored.outlined.Undo
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.AddComment
import androidx.compose.material.icons.outlined.BorderColor
import androidx.compose.material.icons.outlined.Checklist
import androidx.compose.material.icons.outlined.FormatAlignCenter
import androidx.compose.material.icons.outlined.FormatAlignJustify
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatClear
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatLineSpacing
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatPaint
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Spellcheck
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FormattingToolbar(state: EditorState, actions: EditorActions, modifier: Modifier = Modifier) {
    val format = state.format
    val undoState = state.selectedTab.text.undoState
    val editable = state.mode != EditMode.Viewing

    fun update(transform: TextFormat.() -> TextFormat) = actions.format(transform)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(CircleShape)
            .background(DocsColors.ToolbarBackground)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState()),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            ToolbarIconButton(Icons.Outlined.Search, "Search the menus (Alt+/)", onClick = {})
            ToolbarIconButton(Icons.AutoMirrored.Outlined.Undo, "Undo (Ctrl+Z)", onClick = actions::undo, enabled = editable && undoState.canUndo)
            ToolbarIconButton(Icons.AutoMirrored.Outlined.Redo, "Redo (Ctrl+Y)", onClick = actions::redo, enabled = editable && undoState.canRedo)
            ToolbarIconButton(Icons.Outlined.Print, "Print (Ctrl+P)", onClick = {})
            ToolbarIconButton(Icons.Outlined.Spellcheck, "Spelling and grammar check", onClick = {})
            ToolbarIconButton(Icons.Outlined.FormatPaint, "Paint format", onClick = {})
            ZoomDropdown(state.zoom) { state.zoom = it }
            ToolbarSeparator()
            ParagraphStyleDropdown(format.paragraphStyle) { style -> actions.perform { state.applyParagraphStyle(style) } }
            ToolbarSeparator()
            FontDropdown(format.font) { font -> update { copy(font = font) } }
            ToolbarSeparator()
            FontSizeControl(format.fontSize) { size -> actions.perform { state.setFontSize(size) } }
            ToolbarSeparator()
            ToolbarIconButton(Icons.Outlined.FormatBold, "Bold (Ctrl+B)", onClick = { update { copy(bold = !bold) } }, selected = format.bold)
            ToolbarIconButton(Icons.Outlined.FormatItalic, "Italic (Ctrl+I)", onClick = { update { copy(italic = !italic) } }, selected = format.italic)
            ToolbarIconButton(Icons.Outlined.FormatUnderlined, "Underline (Ctrl+U)", onClick = { update { copy(underline = !underline) } }, selected = format.underline)
            TextColorDropdown(format.textColor) { color -> update { copy(textColor = color) } }
            HighlightDropdown(format.highlight) { color -> update { copy(highlight = color) } }
            ToolbarSeparator()
            ToolbarIconButton(Icons.Outlined.Link, "Insert link (Ctrl+K)", onClick = {})
            ToolbarIconButton(Icons.Outlined.AddComment, "Add comment (Ctrl+Alt+M)", onClick = {})
            ToolbarIconButton(Icons.Outlined.Image, "Insert image", onClick = {})
            ToolbarSeparator()
            AlignDropdown(format.align) { align -> update { copy(align = align) } }
            LineSpacingDropdown(format.lineSpacing) { spacing -> update { copy(lineSpacing = spacing) } }
            ListButton(Icons.Outlined.Checklist, "Checklist (Ctrl+Shift+9)") { actions.toggleList(ListKind.Checklist) }
            ListButton(Icons.AutoMirrored.Outlined.FormatListBulleted, "Bulleted list (Ctrl+Shift+8)") { actions.toggleList(ListKind.Bulleted) }
            ListButton(Icons.Outlined.FormatListNumbered, "Numbered list (Ctrl+Shift+7)") { actions.toggleList(ListKind.Numbered) }
            ToolbarIconButton(Icons.AutoMirrored.Outlined.FormatIndentDecrease, "Decrease indent (Ctrl+[)", onClick = actions::outdent)
            ToolbarIconButton(Icons.AutoMirrored.Outlined.FormatIndentIncrease, "Increase indent (Ctrl+])", onClick = actions::indent)
            ToolbarIconButton(Icons.Outlined.FormatClear, "Clear formatting (Ctrl+\\)", onClick = { actions.perform { state.clearFormatting() } })
        }
        EditModeDropdown(state.mode) { state.mode = it }
        ToolbarSeparator()
        ToolbarIconButton(
            icon = if (state.showMenus) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            tooltip = if (state.showMenus) "Hide the menus (Ctrl+Shift+F)" else "Show the menus (Ctrl+Shift+F)",
            onClick = { state.showMenus = !state.showMenus },
        )
    }
}

@Composable
private fun ZoomDropdown(zoom: Float, onZoomChange: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Zoom",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        label = { Text(zoom.asPercent(), style = ToolbarTextStyle) },
    ) {
        ZoomLevels.forEach { level ->
            MenuItem(level.asPercent(), { onZoomChange(level); expanded = false }, checked = level == zoom)
        }
    }
}

@Composable
private fun ParagraphStyleDropdown(style: ParagraphStyle, onSelect: (ParagraphStyle) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Styles (Ctrl+Alt+[0-6])",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.width(112.dp),
        label = { Text(style.label, style = ToolbarTextStyle, maxLines = 1, modifier = Modifier.weight(1f)) },
    ) {
        ParagraphStyle.entries.forEach { entry ->
            MenuItem(
                text = entry.label,
                onClick = { onSelect(entry); expanded = false },
                checked = entry == style,
                textStyle = ToolbarTextStyle.copy(
                    fontSize = (entry.fontSize * 1.1f).coerceAtMost(22f).sp,
                    fontWeight = if (entry.bold) FontWeight.Bold else FontWeight.Normal,
                ),
            )
        }
    }
}

@Composable
private fun FontDropdown(font: DocFont, onSelect: (DocFont) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Font",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.width(112.dp),
        label = { Text(font.label, style = ToolbarTextStyle, maxLines = 1, modifier = Modifier.weight(1f)) },
    ) {
        DocFont.entries.forEach { entry ->
            MenuItem(
                text = entry.label,
                onClick = { onSelect(entry); expanded = false },
                checked = entry == font,
                textStyle = ToolbarTextStyle.copy(fontFamily = entry.family),
            )
        }
    }
}

@Composable
private fun FontSizeControl(size: Int, onSizeChange: (Int) -> Unit) {
    var draft by remember(size) { mutableStateOf(size.toString()) }
    fun commit() = draft.toIntOrNull()?.let(onSizeChange) ?: run { draft = size.toString() }

    Row(verticalAlignment = Alignment.CenterVertically) {
        ToolbarIconButton(Icons.Outlined.Remove, "Decrease font size (Ctrl+Shift+,)", onClick = { onSizeChange(size - 1) })
        WithTooltip("Font size") {
            BasicTextField(
                value = draft,
                onValueChange = { value -> if (value.length <= 3 && value.all(Char::isDigit)) draft = value },
                singleLine = true,
                textStyle = ToolbarTextStyle.copy(textAlign = TextAlign.Center),
                cursorBrush = SolidColor(DocsColors.TextPrimary),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { commit() }),
                modifier = Modifier
                    .width(36.dp)
                    .height(26.dp)
                    .border(1.dp, DocsColors.TextSecondary, RoundedCornerShape(4.dp))
                    .padding(top = 4.dp),
            )
        }
        ToolbarIconButton(Icons.Outlined.Add, "Increase font size (Ctrl+Shift+.)", onClick = { onSizeChange(size + 1) })
    }
}

private val TextColors = listOf(
    Color.Black, Color(0xFF434343), Color(0xFF999999), Color(0xFFCC0000), Color(0xFFE69138),
    Color(0xFFF1C232), Color(0xFF6AA84F), Color(0xFF3C78D8), Color(0xFF674EA7), Color(0xFFA64D79),
)

private val HighlightColors = listOf(
    Color.Unspecified, Color(0xFFFFFF00), Color(0xFF00FF00), Color(0xFF00FFFF), Color(0xFFFF00FF),
    Color(0xFFF4CCCC), Color(0xFFFCE5CD), Color(0xFFD9EAD3), Color(0xFFCFE2F3), Color(0xFFD9D2E9),
)

@Composable
private fun TextColorDropdown(color: Color, onSelect: (Color) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Text color",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        showArrow = false,
        label = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("A", fontSize = 15.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium, color = DocsColors.Icon)
                Box(Modifier.size(width = 16.dp, height = 3.dp).background(color))
            }
        },
    ) {
        ColorGrid(TextColors, selected = color) { onSelect(it); expanded = false }
    }
}

@Composable
private fun HighlightDropdown(color: Color, onSelect: (Color) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Highlight color",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        showArrow = false,
        modifier = Modifier.width(28.dp),
        label = {
            Box(contentAlignment = Alignment.BottomCenter) {
                Icon(Icons.Outlined.BorderColor, contentDescription = "Highlight color", tint = DocsColors.Icon, modifier = Modifier.size(20.dp))
                if (color != Color.Unspecified) Box(Modifier.size(width = 16.dp, height = 3.dp).background(color))
            }
        },
    ) {
        ColorGrid(HighlightColors, selected = color) { onSelect(it); expanded = false }
    }
}

@Composable
private fun ColorGrid(colors: List<Color>, selected: Color, onSelect: (Color) -> Unit) {
    Column(Modifier.padding(horizontal = 12.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        colors.chunked(5).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { swatch ->
                    val isNone = swatch == Color.Unspecified
                    Box(
                        Modifier
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(if (isNone) Color.White else swatch)
                            .border(
                                width = if (swatch == selected) 2.dp else 1.dp,
                                color = if (swatch == selected) DocsColors.SelectedContent else DocsColors.Divider,
                                shape = CircleShape,
                            )
                            .clickable { onSelect(swatch) },
                        contentAlignment = Alignment.Center,
                    ) {
                        if (isNone) Text("∅", fontSize = 12.sp, color = DocsColors.TextSecondary)
                    }
                }
            }
        }
    }
}

private val AlignOptions = listOf(
    Triple(TextAlign.Start, "Left align (Ctrl+Shift+L)", Icons.AutoMirrored.Outlined.FormatAlignLeft),
    Triple(TextAlign.Center, "Center align (Ctrl+Shift+E)", Icons.Outlined.FormatAlignCenter),
    Triple(TextAlign.End, "Right align (Ctrl+Shift+R)", Icons.AutoMirrored.Outlined.FormatAlignRight),
    Triple(TextAlign.Justify, "Justify (Ctrl+Shift+J)", Icons.Outlined.FormatAlignJustify),
)

@Composable
private fun AlignDropdown(align: TextAlign, onSelect: (TextAlign) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val current = AlignOptions.first { it.first == align }
    ToolbarDropdown(
        tooltip = "Align & indent",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        label = { Icon(current.third, contentDescription = current.second, tint = DocsColors.Icon, modifier = Modifier.size(20.dp)) },
    ) {
        AlignOptions.forEach { (option, label, icon) ->
            MenuItem(label.substringBefore(" ("), { onSelect(option); expanded = false }, leadingIcon = icon, checked = option == align)
        }
    }
}

@Composable
private fun LineSpacingDropdown(spacing: Float, onSelect: (Float) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Line & paragraph spacing",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        showArrow = false,
        modifier = Modifier.width(28.dp),
        label = { Icon(Icons.Outlined.FormatLineSpacing, contentDescription = "Line spacing", tint = DocsColors.Icon, modifier = Modifier.size(20.dp)) },
    ) {
        LineSpacings.forEach { option ->
            val label = when (option) {
                1f -> "Single"
                2f -> "Double"
                else -> option.toString()
            }
            MenuItem(label, { onSelect(option); expanded = false }, checked = option == spacing)
        }
    }
}

/** List buttons have a split dropdown arrow in Docs; the arrow is decorative for now. */
@Composable
private fun ListButton(icon: ImageVector, tooltip: String, onClick: () -> Unit) {
    ChromeButton(onClick = onClick, tooltip = tooltip, modifier = Modifier.height(28.dp)) {
        Row(Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = tooltip, tint = DocsColors.Icon, modifier = Modifier.size(20.dp))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, tint = DocsColors.Icon, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun EditModeDropdown(mode: EditMode, onSelect: (EditMode) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    ToolbarDropdown(
        tooltip = "Editing mode",
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier.width(128.dp),
        label = {
            Icon(mode.icon, contentDescription = null, tint = DocsColors.Icon, modifier = Modifier.size(18.dp))
            Text(mode.label, style = ToolbarTextStyle, modifier = Modifier.weight(1f).padding(start = 8.dp))
        },
    ) {
        EditMode.entries.forEach { entry ->
            MenuItem(
                text = "${entry.label} — ${entry.description}",
                onClick = { onSelect(entry); expanded = false },
                leadingIcon = entry.icon,
                checked = entry == mode,
            )
        }
    }
}

private fun Float.asPercent(): String = "${(this * 100).toInt()}%"
