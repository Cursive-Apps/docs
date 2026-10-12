package com.cursorapps.docs.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val ToolbarTextStyle = TextStyle(fontSize = 14.sp, color = DocsColors.TextPrimary)

/** Shows [label] as a tooltip below [content] on hover. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun WithTooltip(label: String?, content: @Composable () -> Unit) {
    if (label == null) {
        content()
        return
    }
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Below),
        tooltip = { PlainTooltip { Text(label) } },
        state = rememberTooltipState(),
    ) {
        content()
    }
}

/** Clickable container with the hover / selected states used across the Docs chrome. */
@Composable
internal fun ChromeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tooltip: String? = null,
    selected: Boolean = false,
    enabled: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp),
    background: Color = Color.Transparent,
    content: @Composable BoxScope.() -> Unit,
) {
    val interaction = remember { MutableInteractionSource() }
    val hovered by interaction.collectIsHoveredAsState()
    WithTooltip(tooltip) {
        Box(
            modifier = modifier
                .clip(shape)
                .background(if (selected) DocsColors.Selected else background)
                .background(if (hovered && enabled) DocsColors.HoverOverlay else Color.Transparent)
                .hoverable(interaction, enabled)
                .clickable(
                    interactionSource = interaction,
                    enabled = enabled,
                    role = Role.Button,
                    onClick = onClick,
                ),
            contentAlignment = Alignment.Center,
            content = content,
        )
    }
}

@Composable
internal fun ToolbarIconButton(
    icon: ImageVector,
    tooltip: String,
    onClick: () -> Unit,
    selected: Boolean = false,
    enabled: Boolean = true,
    size: Dp = 28.dp,
    iconSize: Dp = 20.dp,
) {
    ChromeButton(
        onClick = onClick,
        modifier = Modifier.size(size),
        tooltip = tooltip,
        selected = selected,
        enabled = enabled,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tooltip,
            modifier = Modifier.size(iconSize),
            tint = when {
                !enabled -> DocsColors.Icon.copy(alpha = 0.38f)
                selected -> DocsColors.SelectedContent
                else -> DocsColors.Icon
            },
        )
    }
}

/** Toolbar button that opens a dropdown menu, e.g. zoom, font or alignment pickers. */
@Composable
internal fun ToolbarDropdown(
    tooltip: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    showArrow: Boolean = true,
    label: @Composable RowScope.() -> Unit,
    menu: @Composable () -> Unit,
) {
    Box {
        ChromeButton(
            onClick = { onExpandedChange(!expanded) },
            modifier = modifier.height(28.dp),
            tooltip = tooltip,
            selected = expanded,
        ) {
            Row(
                modifier = Modifier.padding(start = 6.dp, end = if (showArrow) 0.dp else 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                label()
                if (showArrow) {
                    Icon(
                        Icons.Filled.ArrowDropDown,
                        contentDescription = null,
                        tint = DocsColors.Icon,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            containerColor = DocsColors.Page,
        ) {
            menu()
        }
    }
}

@Composable
internal fun MenuItem(
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    trailingText: String? = null,
    checked: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = ToolbarTextStyle,
) {
    DropdownMenuItem(
        text = { Text(text, style = textStyle) },
        onClick = onClick,
        enabled = enabled,
        leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null, modifier = Modifier.size(20.dp)) } },
        trailingIcon = trailingText?.let { { Text(it, fontSize = 12.sp, color = DocsColors.TextSecondary) } },
        modifier = Modifier
            .height(32.dp)
            .background(if (checked) DocsColors.Selected else Color.Transparent),
        colors = MenuDefaults.itemColors(leadingIconColor = DocsColors.Icon),
    )
}

@Composable
internal fun ToolbarSeparator() {
    Box(
        Modifier
            .padding(horizontal = 4.dp)
            .width(1.dp)
            .height(20.dp)
            .background(DocsColors.Divider)
    )
}
