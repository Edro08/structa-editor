package com.edro08.structa.domain.editor.completion

/** Kinds of completion items presented in the suggestion UI. */
enum class CompletionKind { KEYWORD, IDENTIFIER, SNIPPET }

data class CompletionItem(
    val label: String,
    val insertText: String = label,
    val kind: CompletionKind,
    val detail: String = ""
)

/**
 * Pure Kotlin completion engine for word-based, keyword, and snippet autocompletion.
 * Does not depend on external language servers or runtimes.
 */
object CompletionEngine {

    private val IDENTIFIER_REGEX = Regex("""\b[A-Za-z_][A-Za-z0-9_]*\b""")

    /**
     * Finds the start offset and text of the word prefix immediately preceding the cursor.
     */
    fun findPrefix(text: CharSequence, offset: Int): Pair<Int, String> {
        val safeOffset = offset.coerceIn(0, text.length)
        var start = safeOffset
        while (start > 0) {
            val prev = text[start - 1]
            if (prev.isLetterOrDigit() || prev == '_' || prev == '$') {
                start--
            } else {
                break
            }
        }
        val prefix = text.subSequence(start, safeOffset).toString()
        return start to prefix
    }

    /**
     * Produces completion items matching the identifier prefix at the cursor.
     */
    fun complete(
        bufferText: CharSequence,
        offset: Int,
        languageKeywords: Set<String> = emptySet(),
        extraWords: Set<String> = emptySet(),
        limit: Int = 15
    ): List<CompletionItem> {
        val (_, prefix) = findPrefix(bufferText, offset)
        if (prefix.isBlank()) return emptyList()

        val prefixLower = prefix.lowercase(java.util.Locale.ROOT)

        // 1. Collect distinct identifiers from buffer
        val bufferWords = mutableSetOf<String>()
        val matches = IDENTIFIER_REGEX.findAll(bufferText)
        for (m in matches) {
            val word = m.value
            if (word.length > 1 && word != prefix) {
                bufferWords.add(word)
            }
        }
        bufferWords.addAll(extraWords)

        val results = mutableListOf<CompletionItem>()
        val seen = mutableSetOf<String>()

        // 2. Match language keywords
        for (kw in languageKeywords) {
            if (kw.lowercase(java.util.Locale.ROOT).startsWith(prefixLower) && kw != prefix) {
                if (seen.add(kw)) {
                    results += CompletionItem(
                        label = kw,
                        insertText = kw,
                        kind = CompletionKind.KEYWORD,
                        detail = "keyword"
                    )
                }
            }
        }

        // 3. Match identifiers
        for (w in bufferWords) {
            if (w.lowercase(java.util.Locale.ROOT).startsWith(prefixLower) && w !in languageKeywords) {
                if (seen.add(w)) {
                    results += CompletionItem(
                        label = w,
                        insertText = w,
                        kind = CompletionKind.IDENTIFIER,
                        detail = "identifier"
                    )
                }
            }
        }

        // Sort: exact-case prefix matches first, then by length, then alphabetical
        results.sortWith { a, b ->
            val aExact = a.label.startsWith(prefix)
            val bExact = b.label.startsWith(prefix)
            when {
                aExact && !bExact -> -1
                !aExact && bExact -> 1
                a.kind != b.kind -> if (a.kind == CompletionKind.KEYWORD) -1 else 1
                a.label.length != b.label.length -> a.label.length.compareTo(b.label.length)
                else -> a.label.compareTo(b.label)
            }
        }

        return results.take(limit)
    }
}
