package com.edro08.structa.domain.editor.navigation

/** Kinds of symbols recognizable in source code outlines. */
enum class SymbolKind { CLASS, FUNCTION, INTERFACE, PROPERTY, TYPE, HEADING }

data class DocumentSymbol(
    val name: String,
    val kind: SymbolKind,
    val line: Int,
    val offset: Int,
    val detail: String = ""
)

/**
 * Pure Kotlin symbol extractor for code outline navigation.
 * Uses deterministic lexical rules per language without external grammars or parsers.
 */
object DocumentSymbolExtractor {

    const val MAX_ANALYSIS_CHARS = 1_000_000 // ~25,000 lines safety cap

    private val GO_REGEX = Regex("""(?m)^\s*func\s+(?:\([^)]+\)\s+)?([A-Za-z0-9_]+)|^\s*type\s+([A-Za-z0-9_]+)\s+(struct|interface)""")
    private val MARKDOWN_REGEX = Regex("""(?m)^(#{1,6})\s+(.+)$""")

    fun extract(text: CharSequence, languageId: String): List<DocumentSymbol> {
        if (languageId != "go" && languageId != "markdown") return emptyList()

        val symbols = mutableListOf<DocumentSymbol>()
        val clampedText = if (text.length > MAX_ANALYSIS_CHARS) text.subSequence(0, MAX_ANALYSIS_CHARS) else text
        val lineOffsets = computeLineOffsets(clampedText)

        fun offsetToLine(offset: Int): Int {
            val idx = lineOffsets.binarySearch(offset)
            return if (idx >= 0) idx else -idx - 2
        }

        val input = clampedText.toString()

        when (languageId) {
            "go" -> {
                for (match in GO_REGEX.findAll(input)) {
                    val funcName = match.groupValues[1]
                    if (funcName.isNotEmpty()) {
                        val offset = match.range.first
                        symbols += DocumentSymbol(funcName, SymbolKind.FUNCTION, offsetToLine(offset), offset, "func")
                    } else {
                        val typeName = match.groupValues[2]
                        val typeKind = match.groupValues[3]
                        val offset = match.range.first
                        symbols += DocumentSymbol(typeName, if (typeKind == "interface") SymbolKind.INTERFACE else SymbolKind.CLASS,
                            offsetToLine(offset), offset, typeKind)
                    }
                }
            }
            "markdown" -> {
                for (match in MARKDOWN_REGEX.findAll(input)) {
                    val hashes = match.groupValues[1]
                    val headingText = match.groupValues[2].trim()
                    val offset = match.range.first
                    symbols += DocumentSymbol(headingText, SymbolKind.HEADING, offsetToLine(offset), offset, "H${hashes.length}")
                }
            }
        }

        return symbols
    }

    private fun computeLineOffsets(text: CharSequence): List<Int> {
        val offsets = mutableListOf(0)
        var i = 0
        while (i < text.length) {
            if (text[i] == '\n') {
                offsets.add(i + 1)
            } else if (text[i] == '\r') {
                if (i + 1 < text.length && text[i + 1] == '\n') {
                    offsets.add(i + 2)
                    i++
                } else {
                    offsets.add(i + 1)
                }
            }
            i++
        }
        return offsets
    }
}
