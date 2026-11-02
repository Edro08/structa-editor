package com.edro08.structa.domain.editor.command

import com.edro08.structa.domain.editor.EditorEngine
import com.edro08.structa.domain.editor.buffer.PieceTableBuffer
import com.edro08.structa.domain.editor.cursor.Selection
import com.edro08.structa.domain.editor.cursor.TextOffset
import com.edro08.structa.domain.editor.document.EditorDocument
import org.junit.Assert.assertEquals
import org.junit.Test

class SmartEditCommandsTest {

    private fun engine(initial: String = ""): EditorEngine {
        val buffer = PieceTableBuffer(initial)
        val document = EditorDocument(buffer)
        return EditorEngine(document)
    }

    @Test
    fun autoCloseParenthesesAndBraces() {
        val editor = engine()
        editor.execute(SmartInsertTextCommand("("))
        assertEquals("()", editor.document.buffer.getText(0, 2).toString())
        assertEquals(1, editor.cursor.offset.value)

        val editor2 = engine()
        editor2.execute(SmartInsertTextCommand("{"))
        assertEquals("{}", editor2.document.buffer.getText(0, 2).toString())
        assertEquals(1, editor2.cursor.offset.value)
    }

    @Test
    fun overtypeClosingCharacter() {
        val editor = engine()
        editor.execute(SmartInsertTextCommand("("))
        assertEquals("()", editor.document.buffer.getText(0, 2).toString())
        assertEquals(1, editor.cursor.offset.value)

        // Overtyping ')' should not duplicate it
        editor.execute(SmartInsertTextCommand(")"))
        assertEquals("()", editor.document.buffer.getText(0, 2).toString())
        assertEquals(2, editor.cursor.offset.value)
    }

    @Test
    fun selectionWrappingWithQuotesAndBrackets() {
        val editor = engine("hello")
        editor.execute(SetSelectionCommand(TextOffset(0), TextOffset(5)))
        editor.execute(SmartInsertTextCommand("\""))
        assertEquals("\"hello\"", editor.document.buffer.getText(0, 7).toString())
        assertEquals(Selection(TextOffset(1), TextOffset(6)), editor.selection)

        val editor2 = engine("item")
        editor2.execute(SetSelectionCommand(TextOffset(0), TextOffset(4)))
        editor2.execute(SmartInsertTextCommand("["))
        assertEquals("[item]", editor2.document.buffer.getText(0, 6).toString())
    }

    @Test
    fun mirrorDeletionOfEmptyPairs() {
        val editor = engine()
        editor.execute(SmartInsertTextCommand("("))
        assertEquals("()", editor.document.buffer.getText(0, 2).toString())
        assertEquals(1, editor.cursor.offset.value)

        editor.execute(SmartDeleteBackwardCommand)
        assertEquals("", editor.document.buffer.getText(0, editor.document.buffer.length).toString())
        assertEquals(0, editor.cursor.offset.value)
    }

    @Test
    fun smartEnterAutoIndentation() {
        val editor = engine("fun main() {")
        editor.execute(MoveCursorCommand(TextOffset(12)))
        editor.execute(SmartInsertTextCommand("\n"))
        assertEquals("fun main() {\n    ", editor.document.buffer.getText(0, editor.document.buffer.length).toString())
        assertEquals(17, editor.cursor.offset.value)
    }

    @Test
    fun smartEnterBetweenBraces() {
        val editor = engine("{}")
        editor.execute(MoveCursorCommand(TextOffset(1)))
        editor.execute(SmartInsertTextCommand("\n"))
        val expected = "{\n    \n}"
        assertEquals(expected, editor.document.buffer.getText(0, editor.document.buffer.length).toString())
        assertEquals(6, editor.cursor.offset.value)
    }

    @Test
    fun duplicateLineDown() {
        val editor = engine("line 1")
        editor.execute(DuplicateLineCommand(down = true))
        assertEquals("line 1\nline 1", editor.document.buffer.getText(0, editor.document.buffer.length).toString())
    }

    @Test
    fun moveLineDownAndUp() {
        val editor = engine("first\nsecond")
        editor.execute(MoveCursorCommand(TextOffset(0)))
        editor.execute(MoveLineCommand(down = true))
        assertEquals("second\nfirst", editor.document.buffer.getText(0, editor.document.buffer.length).toString())

        editor.execute(MoveLineCommand(down = false))
        assertEquals("first\nsecond", editor.document.buffer.getText(0, editor.document.buffer.length).toString())
    }

    @Test
    fun deleteLine() {
        val editor = engine("line 1\nline 2\nline 3")
        editor.execute(MoveCursorCommand(TextOffset(8))) // on line 2
        editor.execute(DeleteLineCommand)
        assertEquals("line 1\nline 3", editor.document.buffer.getText(0, editor.document.buffer.length).toString())
    }

    @Test
    fun indentAndUnindentLines() {
        val editor = engine("hello")
        editor.execute(IndentLinesCommand(unindent = false, indentSpaces = 4))
        assertEquals("    hello", editor.document.buffer.getText(0, editor.document.buffer.length).toString())

        editor.execute(IndentLinesCommand(unindent = true, indentSpaces = 4))
        assertEquals("hello", editor.document.buffer.getText(0, editor.document.buffer.length).toString())
    }
}
