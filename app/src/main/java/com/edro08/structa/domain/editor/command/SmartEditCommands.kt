package com.edro08.structa.domain.editor.command

import com.edro08.structa.domain.editor.cursor.Cursor
import com.edro08.structa.domain.editor.cursor.TextOffset
import com.edro08.structa.domain.editor.cursor.TextRange

/**
 * Smart code editing commands:
 * - Auto-closing pairs and overtyping: (), [], {}, "", '', ``
 * - Selection wrapping with delimiters
 * - Smart auto-indentation on Enter (after {, :, etc.)
 * - Mirror deletion of empty bracket pairs on Backspace
 * - Line operations: Comment toggle, Duplicate line, Move line, Delete line, Indent/Unindent
 */

private val PAIR_MAP = mapOf(
    '(' to ')',
    '[' to ']',
    '{' to '}',
    '"' to '"',
    '\'' to '\'',
    '`' to '`'
)

private val CLOSING_CHARS = setOf(')', ']', '}', '"', '\'', '`')

data class SmartInsertTextCommand(
    val text: String,
    val indentSize: Int = 4
) : EditorCommand {
    override fun execute(context: EditorContext) {
        val session = context.session
        val buffer = session.buffer
        val selection = session.selection
        val cursorOffset = session.cursor.offset.value

        // 1. Enter key: Smart auto-indentation
        if (text == "\n") {
            session.beginTransaction()
            try {
                val currentLine = buffer.getLineForOffset(cursorOffset)
                val lineStart = buffer.getLineStart(currentLine)
                val lineText = buffer.getText(lineStart, cursorOffset).toString()

                // Calculate leading indent of current line
                val leadingWhitespace = lineText.takeWhile { it == ' ' || it == '\t' }
                val trimmedBefore = lineText.trimEnd()
                val lastChar = trimmedBefore.lastOrNull()

                val charAfterCursor = if (cursorOffset < buffer.length) buffer.charAt(cursorOffset) else null

                // Special case: pressing Enter between { and }
                if (lastChar == '{' && charAfterCursor == '}') {
                    val indentStep = " ".repeat(indentSize)
                    val insertion = "\n$leadingWhitespace$indentStep\n$leadingWhitespace"
                    session.insertText(insertion)
                    val targetCursor = cursorOffset + 1 + leadingWhitespace.length + indentStep.length
                    session.moveTo(TextOffset(targetCursor))
                } else if (lastChar in setOf('{', '(', '[', ':')) {
                    // Increase indent
                    val indentStep = " ".repeat(indentSize)
                    session.insertText("\n$leadingWhitespace$indentStep")
                } else {
                    // Preserve base indent
                    session.insertText("\n$leadingWhitespace")
                }
            } finally {
                session.commitTransaction()
            }
            return
        }

        // 2. Selection wrapping
        if (selection != null && text.length == 1) {
            val char = text[0]
            val closing = PAIR_MAP[char]
            if (closing != null) {
                val range = selection.range
                val selectedContent = buffer.getText(range.start.value, range.end.value)
                val wrapped = "$char$selectedContent$closing"
                session.replace(range, wrapped)
                // Select the wrapped text
                session.select(
                    TextOffset(range.start.value + 1),
                    TextOffset(range.start.value + 1 + selectedContent.length)
                )
                return
            }
        }

        // 3. Single-character auto-close & overtyping
        if (selection == null && text.length == 1) {
            val char = text[0]
            val charAfterCursor = if (cursorOffset < buffer.length) buffer.charAt(cursorOffset) else null

            // Overtype: if typing the exact closing character already present right after cursor
            if (char in CLOSING_CHARS && charAfterCursor == char) {
                session.moveTo(TextOffset(cursorOffset + 1))
                return
            }

            // Auto-closing open pairs
            val closing = PAIR_MAP[char]
            if (closing != null) {
                val charBeforeCursor = if (cursorOffset > 0) buffer.charAt(cursorOffset - 1) else null
                val shouldClose = when (char) {
                    '"', '\'', '`' -> charBeforeCursor == null || charBeforeCursor.isWhitespace() || charBeforeCursor in "([{=,;:"
                    else -> true
                }

                if (shouldClose) {
                    session.insertText("$char$closing")
                    session.moveTo(TextOffset(cursorOffset + 1))
                    return
                }
            }
        }

        // Default insertion
        session.insertText(text)
    }
}

object SmartDeleteBackwardCommand : EditorCommand {
    override fun execute(context: EditorContext) {
        val session = context.session
        val buffer = session.buffer
        val selection = session.selection
        val offset = session.cursor.offset.value

        // If no selection, check for mirror deletion of empty pairs: (), [], {}, "", '', ``
        if (selection == null && offset > 0 && offset < buffer.length) {
            val before = buffer.charAt(offset - 1)
            val after = buffer.charAt(offset)
            if (PAIR_MAP[before] == after) {
                session.replace(TextRange(TextOffset(offset - 1), TextOffset(offset + 1)), "")
                return
            }
        }

        session.deleteBackward()
    }
}

data class DuplicateLineCommand(val down: Boolean = true) : EditorCommand {
    override fun execute(context: EditorContext) {
        val session = context.session
        val buffer = session.buffer
        val line = buffer.getLineForOffset(session.cursor.offset.value)
        val start = buffer.getLineStart(line)
        val end = buffer.getLineEnd(line)
        val lineText = buffer.getText(start, end).toString()

        session.beginTransaction()
        try {
            if (down) {
                session.replace(TextRange(TextOffset(end), TextOffset(end)), "\n$lineText")
                session.moveTo(TextOffset(end + 1 + lineText.length))
            } else {
                session.replace(TextRange(TextOffset(start), TextOffset(start)), "$lineText\n")
                session.moveTo(TextOffset(start))
            }
        } finally {
            session.commitTransaction()
        }
    }
}

data class MoveLineCommand(val down: Boolean = true) : EditorCommand {
    override fun execute(context: EditorContext) {
        val session = context.session
        val buffer = session.buffer
        val currentLine = buffer.getLineForOffset(session.cursor.offset.value)
        val targetLine = if (down) currentLine + 1 else currentLine - 1

        if (targetLine !in 0 until buffer.lineCount) return

        val cursorCol = session.cursor.offset.value - buffer.getLineStart(currentLine)

        val currentStart = buffer.getLineStart(currentLine)
        val currentEnd = buffer.getLineEnd(currentLine)
        val currentText = buffer.getText(currentStart, currentEnd).toString()

        val targetStart = buffer.getLineStart(targetLine)
        val targetEnd = buffer.getLineEnd(targetLine)
        val targetText = buffer.getText(targetStart, targetEnd).toString()

        session.beginTransaction()
        try {
            if (down) {
                // Moving down: replace range from currentStart to targetEnd with targetText + "\n" + currentText
                session.replace(TextRange(TextOffset(currentStart), TextOffset(targetEnd)), "$targetText\n$currentText")
                val newTargetStart = currentStart + targetText.length + 1
                val newCursor = newTargetStart + cursorCol.coerceAtMost(currentText.length)
                session.moveTo(TextOffset(newCursor))
            } else {
                // Moving up: replace range from targetStart to currentEnd with currentText + "\n" + targetText
                session.replace(TextRange(TextOffset(targetStart), TextOffset(currentEnd)), "$currentText\n$targetText")
                val newCursor = targetStart + cursorCol.coerceAtMost(currentText.length)
                session.moveTo(TextOffset(newCursor))
            }
        } finally {
            session.commitTransaction()
        }
    }
}

object DeleteLineCommand : EditorCommand {
    override fun execute(context: EditorContext) {
        val session = context.session
        val buffer = session.buffer
        val line = buffer.getLineForOffset(session.cursor.offset.value)
        val start = buffer.getLineStart(line)
        val end = buffer.getLineEnd(line)

        val deleteEnd = if (end < buffer.length) end + 1 else end
        val deleteStart = if (start > 0 && deleteEnd == buffer.length) start - 1 else start

        session.replace(TextRange(TextOffset(deleteStart), TextOffset(deleteEnd)), "")
    }
}

data class IndentLinesCommand(
    val unindent: Boolean = false,
    val indentSpaces: Int = 4
) : EditorCommand {
    override fun execute(context: EditorContext) {
        val session = context.session
        val buffer = session.buffer
        val selection = session.selection
        val indentStr = " ".repeat(indentSpaces)

        val (startLine, endLine) = if (selection != null) {
            val start = buffer.getLineForOffset(selection.range.start.value)
            val end = buffer.getLineForOffset(selection.range.end.value)
            val adjustedEnd = if (end > start && selection.range.end.value == buffer.getLineStart(end)) end - 1 else end
            start to adjustedEnd
        } else {
            val line = buffer.getLineForOffset(session.cursor.offset.value)
            line to line
        }

        session.beginTransaction()
        try {
            for (lineIndex in endLine downTo startLine) {
                val start = buffer.getLineStart(lineIndex)
                val end = buffer.getLineEnd(lineIndex)
                val text = buffer.getText(start, end).toString()
                val lineRange = TextRange(TextOffset(start), TextOffset(end))

                if (unindent) {
                    if (text.startsWith(indentStr)) {
                        session.replace(lineRange, text.removePrefix(indentStr))
                    } else if (text.startsWith("\t")) {
                        session.replace(lineRange, text.removePrefix("\t"))
                    } else {
                        val leadingSpaces = text.takeWhile { it == ' ' }
                        if (leadingSpaces.isNotEmpty()) {
                            session.replace(lineRange, text.drop(leadingSpaces.length))
                        }
                    }
                } else {
                    session.replace(lineRange, indentStr + text)
                }
            }
        } finally {
            session.commitTransaction()
        }
    }
}
