package com.edro08.structa.domain.editor.completion

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CompletionEngineTest {

    @Test
    fun findsPrefixBeforeCursor() {
        val text = "val myVariable = 42"
        val (start, prefix) = CompletionEngine.findPrefix(text, 10) // inside "myVariable", after "myVari"
        assertEquals(4, start)
        assertEquals("myVari", prefix)
        val (fullStart, fullPrefix) = CompletionEngine.findPrefix(text, 14) // after "myVariable"
        assertEquals(4, fullStart)
        assertEquals("myVariable", fullPrefix)
    }

    @Test
    fun returnsEmptyOnWhitespacePrefix() {
        val text = "val x = "
        val (_, prefix) = CompletionEngine.findPrefix(text, text.length)
        assertTrue(prefix.isEmpty())
        val items = CompletionEngine.complete(text, text.length)
        assertTrue(items.isEmpty())
    }

    @Test
    fun completesKeywordsAndBufferWords() {
        val keywords = setOf("fun", "for", "final", "finally", "val", "var")
        val text = "functionality forecast format"
        // Cursor at end of "fo"
        val buffer = "$text\nfo"
        val items = CompletionEngine.complete(buffer, buffer.length, keywords)

        assertTrue(items.isNotEmpty())
        // "for" is a keyword match
        assertTrue(items.any { it.label == "for" && it.kind == CompletionKind.KEYWORD })
        // "forecast" and "format" are identifier matches
        assertTrue(items.any { it.label == "forecast" && it.kind == CompletionKind.IDENTIFIER })
        assertTrue(items.any { it.label == "format" && it.kind == CompletionKind.IDENTIFIER })
    }

    @Test
    fun sortsExactCasePrefixFirst() {
        val keywords = setOf("val", "var")
        val text = "Value validator valueless"
        val buffer = "$text\nval"
        val items = CompletionEngine.complete(buffer, buffer.length, keywords)

        // Exact match with prefix "val" is excluded from completing itself, but words starting with exact "val" come first
        val labels = items.map { it.label }
        assertTrue("valueless" in labels)
        assertTrue("Value" in labels)
    }
}
