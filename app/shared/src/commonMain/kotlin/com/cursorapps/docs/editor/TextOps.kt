package com.cursorapps.docs.editor

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.text.TextRange

/** Plain-text list markers until the editor supports real paragraph formatting. */
internal enum class ListKind {
    Bulleted, Numbered, Checklist;

    fun marker(index: Int): String = when (this) {
        Bulleted -> "• "
        Numbered -> "${index + 1}. "
        Checklist -> "☐ "
    }
}

private const val INDENT = "    "
private val linePattern = Regex("""^( *)(• |☐ |☑ |\d+\. )?(.*)$""")

private data class ParsedLine(val indent: String, val marker: String?, val body: String)

private fun parse(line: String): ParsedLine {
    val match = linePattern.matchEntire(line)!!
    val marker = match.groupValues[2].ifEmpty { null }
    return ParsedLine(match.groupValues[1], marker, match.groupValues[3])
}

private fun ListKind.matches(marker: String?): Boolean = when (this) {
    ListKind.Bulleted -> marker == "• "
    ListKind.Numbered -> marker != null && marker.first().isDigit()
    ListKind.Checklist -> marker == "☐ " || marker == "☑ "
}

/** Adds [kind] markers to every line touched by the selection, or removes them if all lines already have them. */
internal fun TextFieldState.toggleList(kind: ListKind) {
    transformSelectedLines { lines ->
        val parsed = lines.map(::parse)
        val remove = parsed.all { kind.matches(it.marker) }
        parsed.mapIndexed { i, line ->
            if (remove) line.indent + line.body else line.indent + kind.marker(i) + line.body
        }
    }
}

internal fun TextFieldState.indentLines() {
    transformSelectedLines { lines -> lines.map { INDENT + it } }
}

internal fun TextFieldState.outdentLines() {
    transformSelectedLines { lines ->
        lines.map { it.drop(it.takeWhile { c -> c == ' ' }.length.coerceAtMost(INDENT.length)) }
    }
}

/**
 * Replaces the full lines covered by the current selection with the result of [transform],
 * keeping the cursor on the same line.
 */
private fun TextFieldState.transformSelectedLines(transform: (List<String>) -> List<String>) {
    val current = text.toString()
    val sel = selection
    val blockStart = if (sel.min == 0) 0 else current.lastIndexOf('\n', sel.min - 1) + 1
    val blockEnd = current.indexOf('\n', sel.max).let { if (it < 0) current.length else it }
    val oldLines = current.substring(blockStart, blockEnd).split('\n')
    val newLines = transform(oldLines)
    val replacement = newLines.joinToString("\n")

    edit {
        replace(blockStart, blockEnd, replacement)
        selection = if (sel.collapsed) {
            val delta = newLines.first().length - oldLines.first().length
            TextRange((sel.start + delta).coerceIn(blockStart, blockStart + newLines.first().length))
        } else {
            TextRange(blockStart, blockStart + replacement.length)
        }
    }
}
