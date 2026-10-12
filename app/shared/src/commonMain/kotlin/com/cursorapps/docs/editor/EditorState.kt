package com.cursorapps.docs.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign

const val DEFAULT_DOCUMENT_TITLE = "Untitled document"

enum class DocFont(val label: String, val family: FontFamily) {
    Arial("Arial", FontFamily.SansSerif),
    Calibri("Calibri", FontFamily.SansSerif),
    Georgia("Georgia", FontFamily.Serif),
    TimesNewRoman("Times New Roman", FontFamily.Serif),
    CourierNew("Courier New", FontFamily.Monospace),
}

enum class ParagraphStyle(val label: String, val fontSize: Int, val bold: Boolean) {
    NormalText("Normal text", 11, false),
    Title("Title", 26, false),
    Subtitle("Subtitle", 15, false),
    Heading1("Heading 1", 20, false),
    Heading2("Heading 2", 16, false),
    Heading3("Heading 3", 14, true),
}

enum class EditMode(val label: String, val description: String, val icon: ImageVector) {
    Editing("Editing", "Edit document directly", Icons.Outlined.Edit),
    Suggesting("Suggesting", "Edits become suggestions", Icons.Outlined.RateReview),
    Viewing("Viewing", "Read or print final document", Icons.Outlined.Visibility),
}

val ZoomLevels = listOf(0.5f, 0.75f, 0.9f, 1f, 1.25f, 1.5f, 2f)
val LineSpacings = listOf(1f, 1.15f, 1.5f, 2f)

/**
 * Formatting for the document body. For now this applies to the whole tab;
 * per-range (rich text) formatting will replace it later.
 */
data class TextFormat(
    val font: DocFont = DocFont.Arial,
    val paragraphStyle: ParagraphStyle = ParagraphStyle.NormalText,
    val fontSize: Int = ParagraphStyle.NormalText.fontSize,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val textColor: Color = Color.Black,
    val highlight: Color = Color.Unspecified,
    val align: TextAlign = TextAlign.Start,
    val lineSpacing: Float = 1.15f,
)

class DocumentTab(val id: Int, title: String) {
    var title by mutableStateOf(title)
    val text = TextFieldState()
    var format by mutableStateOf(TextFormat())
}

class EditorState {
    var title by mutableStateOf(DEFAULT_DOCUMENT_TITLE)
    var starred by mutableStateOf(false)

    val tabs = mutableStateListOf(DocumentTab(id = 1, title = "Tab 1"))
    var selectedTabId by mutableStateOf(1)
        private set
    val selectedTab: DocumentTab get() = tabs.first { it.id == selectedTabId }

    var zoom by mutableStateOf(1f)
    var mode by mutableStateOf(EditMode.Editing)
    var showOutline by mutableStateOf(true)
    var showMenus by mutableStateOf(true)

    private var nextTabId = 2

    var format: TextFormat
        get() = selectedTab.format
        set(value) {
            selectedTab.format = value
        }

    fun selectTab(id: Int) {
        if (tabs.any { it.id == id }) selectedTabId = id
    }

    fun addTab() {
        val tab = DocumentTab(id = nextTabId++, title = "Tab ${tabs.size + 1}")
        tabs += tab
        selectedTabId = tab.id
    }

    fun deleteTab(id: Int) {
        if (tabs.size <= 1) return
        val index = tabs.indexOfFirst { it.id == id }
        if (index < 0) return
        tabs.removeAt(index)
        if (selectedTabId == id) selectedTabId = tabs[(index - 1).coerceAtLeast(0)].id
    }

    fun setFontSize(size: Int) {
        format = format.copy(fontSize = size.coerceIn(MIN_FONT_SIZE, MAX_FONT_SIZE))
    }

    fun applyParagraphStyle(style: ParagraphStyle) {
        format = format.copy(paragraphStyle = style, fontSize = style.fontSize, bold = style.bold)
    }

    fun clearFormatting() {
        format = TextFormat(paragraphStyle = format.paragraphStyle, fontSize = format.paragraphStyle.fontSize)
    }

    companion object {
        const val MIN_FONT_SIZE = 1
        const val MAX_FONT_SIZE = 400
    }
}
