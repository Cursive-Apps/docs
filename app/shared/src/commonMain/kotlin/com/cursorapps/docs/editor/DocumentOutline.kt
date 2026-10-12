package com.cursorapps.docs.editor

import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val OutlineWidth = 280.dp

/** Left sidebar listing the document's tabs. */
@Composable
internal fun DocumentOutline(state: EditorState, modifier: Modifier = Modifier) {
    Column(modifier.width(OutlineWidth).padding(start = 24.dp, end = 16.dp, top = 12.dp)) {
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            tooltip = "Hide tabs & outlines",
            onClick = { state.showOutline = false },
            size = 40.dp,
            iconSize = 24.dp,
        )
        Spacer(Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Document tabs",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = DocsColors.TextPrimary,
                modifier = Modifier.weight(1f),
            )
            ToolbarIconButton(Icons.Outlined.Add, "Add tab", onClick = state::addTab, size = 36.dp, iconSize = 22.dp)
        }
        Spacer(Modifier.height(12.dp))
        state.tabs.forEach { tab ->
            val selected = tab.id == state.selectedTabId
            TabItem(
                tab = tab,
                selected = selected,
                canDelete = state.tabs.size > 1,
                onSelect = { state.selectTab(tab.id) },
                onDelete = { state.deleteTab(tab.id) },
            )
            if (selected) {
                Text(
                    "Headings you add to the document will appear here.",
                    fontSize = 14.sp,
                    fontStyle = FontStyle.Italic,
                    color = DocsColors.TextSecondary,
                    lineHeight = 18.sp,
                    modifier = Modifier.padding(start = 8.dp, top = 12.dp, bottom = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: DocumentTab,
    selected: Boolean,
    canDelete: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    var menuOpen by remember { mutableStateOf(false) }
    val contentColor = if (selected) DocsColors.SelectedContent else DocsColors.TextPrimary

    ChromeButton(
        onClick = onSelect,
        selected = selected,
        shape = CircleShape,
        modifier = Modifier.fillMaxWidth().height(40.dp).hoverable(interaction),
    ) {
        Row(Modifier.padding(start = 16.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.AutoMirrored.Outlined.Article, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Text(
                tab.title,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
                color = contentColor,
                maxLines = 1,
                modifier = Modifier.weight(1f).padding(start = 12.dp),
            )
            if (selected || hovered || menuOpen) {
                Box {
                    ToolbarIconButton(Icons.Filled.MoreVert, "Tab options", onClick = { menuOpen = true }, size = 32.dp)
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }, containerColor = DocsColors.Page) {
                        MenuItem("Delete", { menuOpen = false; onDelete() }, enabled = canDelete)
                    }
                }
            }
        }
    }
}

/** Shown in place of the sidebar when it's collapsed. */
@Composable
internal fun ShowOutlineButton(state: EditorState, modifier: Modifier = Modifier) {
    Box(modifier.padding(start = 24.dp, top = 12.dp)) {
        ToolbarIconButton(
            icon = Icons.AutoMirrored.Outlined.FormatListBulleted,
            tooltip = "Show tabs & outlines",
            onClick = { state.showOutline = true },
            size = 40.dp,
            iconSize = 24.dp,
        )
    }
}
