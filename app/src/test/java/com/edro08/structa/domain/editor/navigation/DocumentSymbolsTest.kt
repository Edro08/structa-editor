package com.edro08.structa.domain.editor.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DocumentSymbolsTest {

    @Test
    fun extractsGoSymbols() {
        val code = """
            package main
            type Server struct {
                port int
            }
            type Handler interface {
                Serve()
            }
            func Start() {}
        """.trimIndent()

        val symbols = DocumentSymbolExtractor.extract(code, "go")
        assertEquals(3, symbols.size)
        assertEquals("Server", symbols[0].name)
        assertEquals(SymbolKind.CLASS, symbols[0].kind)
        assertEquals("Handler", symbols[1].name)
        assertEquals(SymbolKind.INTERFACE, symbols[1].kind)
        assertEquals("Start", symbols[2].name)
        assertEquals(SymbolKind.FUNCTION, symbols[2].kind)
    }

    @Test
    fun extractsMarkdownHeadings() {
        val doc = """
            # Project Structa
            Introduction text...
            ## Installation
            Steps...
            ### Requirements
            - Android 10+
        """.trimIndent()

        val symbols = DocumentSymbolExtractor.extract(doc, "markdown")
        assertEquals(3, symbols.size)
        assertEquals("Project Structa", symbols[0].name)
        assertEquals("H1", symbols[0].detail)
        assertEquals("Installation", symbols[1].name)
        assertEquals("H2", symbols[1].detail)
        assertEquals("Requirements", symbols[2].name)
        assertEquals("H3", symbols[2].detail)
    }

    @Test
    fun jsonAndYamlDoNotExtractOutline() {
        val json = """{"id": 1, "name": "test", "items": [{"id": 2}]}"""
        assertEquals(0, DocumentSymbolExtractor.extract(json, "json").size)

        val yaml = """
            title: App
            database:
              host: localhost
        """.trimIndent()
        assertEquals(0, DocumentSymbolExtractor.extract(yaml, "yaml").size)
    }

    @Test
    fun offsetToLineAccuracy() {
        val code = "line0\nline1\nfunc target() {}\nline3"
        val symbols = DocumentSymbolExtractor.extract(code, "go")
        assertEquals(1, symbols.size)
        assertEquals("target", symbols[0].name)
        assertEquals(2, symbols[0].line)
    }
}
