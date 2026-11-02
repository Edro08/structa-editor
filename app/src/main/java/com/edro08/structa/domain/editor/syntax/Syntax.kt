package com.edro08.structa.domain.editor.syntax

/** Line-local, half-open UTF-16 ranges. No visual metadata is stored in the buffer. */
enum class SyntaxStyle { KEYWORD, STRING, NUMBER, COMMENT, OPERATOR, KEY, HEADING }
data class SyntaxSpan(val start: Int, val end: Int, val style: SyntaxStyle) {
    init { require(start >= 0 && end > start) }
}
data class TokenizerState(val commentDepth: Int = 0, val delimiter: String = "", val fence: String = "")
data class TokenizationResult(val spans: List<SyntaxSpan>, val state: TokenizerState)
interface LanguageTokenizer {
    fun tokenize(text: CharSequence, state: TokenizerState = TokenizerState(),
        checkCancelled: () -> Unit = {}): TokenizationResult
}
data class Language(
    val id: String,
    val title: String,
    val extensions: Set<String>,
    val tokenizer: LanguageTokenizer,
    val keywords: Set<String> = emptySet(),
    val commentPrefix: String = "// "
)

/** Filename detection belongs here, never in the renderer. Unknown extensions use plain text. */
object LanguageRegistry {
    val plain = Language("text", "Plain Text", setOf("txt"), object : LanguageTokenizer {
        override fun tokenize(text: CharSequence, state: TokenizerState, checkCancelled: () -> Unit) =
            TokenizationResult(emptyList(), TokenizerState())
    })

    private fun code(id: String, title: String, extensions: Set<String>, keywords: String, commentPrefix: String = "// "): Language {
        val wordSet = keywords.split(' ').filter { it.isNotBlank() }.toSet()
        return Language(id, title, extensions, CodeTokenizer(id, wordSet), wordSet, commentPrefix)
    }

    val goLang = code("go", "Go", setOf("go"),
        "break case chan const continue default defer else fallthrough for func go goto if import interface map package range return select struct switch type var true false nil",
        "// ")

    val jsonLang = code("json", "JSON", setOf("json", "jsonl"), "true false null")
    val yamlLang = code("yaml", "YAML", setOf("yaml", "yml"), "true false null yes no on off", "# ")
    val markdownLang = Language("markdown", "Markdown", setOf("md", "markdown"), MarkdownTokenizer, commentPrefix = "> ")
    val xmlLang = Language("xml", "XML", setOf("xml", "svg", "xsd", "xsl", "xslt"), XmlTokenizer, commentPrefix = "<!-- ")

    val languages = listOf(
        plain, markdownLang, jsonLang, yamlLang, xmlLang, goLang
    )

    fun forFileName(name: String, firstLine: String = ""): Language {
        val lower = name.lowercase(java.util.Locale.ROOT)
        val extension = lower.substringAfterLast('.', "")
        return languages.firstOrNull { extension in it.extensions } ?: plain
    }
}

/** Small deterministic lexical highlighter, not a compiler/parser. Loops are cancellation-aware. */
internal class CodeTokenizer(private val kind: String, private val words: Set<String>) : LanguageTokenizer {
    constructor(kind: String, keywords: String) : this(kind, keywords.split(' ').filter { it.isNotBlank() }.toSet())

    override fun tokenize(text: CharSequence, state: TokenizerState, checkCancelled: () -> Unit): TokenizationResult {
        val spans = mutableListOf<SyntaxSpan>()
        var depth = state.commentDepth
        var delimiter = state.delimiter
        var i = 0
        var work = 0
        fun at(s: String): Boolean = i + s.length <= text.length && s.indices.all { text[i + it] == s[it] }
        fun tick() { if (work++ % 256 == 0) checkCancelled() }
        fun emit(start: Int, style: SyntaxStyle) { if (i > start) spans += SyntaxSpan(start, i, style) }
        fun comment() {
            while (i < text.length) {
                tick()
                if (at("*/")) { i += 2; depth--; if (depth == 0) break }
                else i++
            }
        }
        fun string() {
            val quote = delimiter
            while (i < text.length) {
                tick()
                if (at(quote)) {
                    i += quote.length
                    if (kind == "yaml" && quote == "'" && at("'")) { i++; continue }
                    delimiter = ""; break
                }
                if (text[i] == '\\' && quote.length == 1 && quote != "`" && !(kind == "yaml" && quote == "'"))
                    i = (i + 2).coerceAtMost(text.length)
                else i++
            }
            // Multi-line quotes keep delimiter across lines: """ , ''' , ` , YAML quotes.
            // Ordinary single-line code quotes do not leak across lines on unclosed quote.
            val isMultilineQuote = delimiter.length == 3 || delimiter == "`" || kind == "yaml"
            if (!isMultilineQuote && delimiter.isNotEmpty()) delimiter = ""
        }
        while (i < text.length) {
            checkCancelled()
            val start = i
            if (depth > 0) { comment(); emit(start, SyntaxStyle.COMMENT); continue }
            if (delimiter.isNotEmpty()) { string(); emit(start, SyntaxStyle.STRING); continue }
            val c = text[i]
            when {
                c.isWhitespace() -> i++
                // Hash comments
                (kind in setOf("python", "shell") && c == '#') ||
                (kind == "yaml" && c == '#' && (i == 0 || text[i - 1].isWhitespace())) -> {
                    i = text.length; emit(start, SyntaxStyle.COMMENT)
                }
                // Preprocessor directives in C/C++
                kind == "cpp" && c == '#' -> {
                    i++
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) { tick(); i++ }
                    emit(start, SyntaxStyle.KEYWORD)
                }
                // C-style single-line comments
                kind !in setOf("python", "shell", "yaml") && at("//") -> {
                    i = text.length; emit(start, SyntaxStyle.COMMENT)
                }
                // C-style multi-line comments
                kind !in setOf("python", "shell", "yaml") && at("/*") -> {
                    depth = 1; i += 2; comment(); emit(start, SyntaxStyle.COMMENT)
                }
                // Triple-quoted multi-line strings
                (kind in setOf("python", "kotlin", "java") && at("\"\"\"")) -> {
                    delimiter = "\"\"\""; i += 3; string(); emit(start, SyntaxStyle.STRING)
                }
                (kind == "python" && at("'''")) -> {
                    delimiter = "'''"; i += 3; string(); emit(start, SyntaxStyle.STRING)
                }
                // Annotations/decorators (@annotation)
                c == '@' && kind in setOf("kotlin", "java", "python", "typescript") -> {
                    i++
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] in "_.")) { tick(); i++ }
                    emit(start, SyntaxStyle.KEYWORD)
                }
                // Regular strings / template literals
                c == '"' || (c == '\'' && kind != "json") || (c == '`' && kind in setOf("go", "javascript", "typescript")) -> {
                    // In Rust, 'a lifetime shouldn't be treated as unclosed string
                    if (kind == "rust" && c == '\'' && i + 1 < text.length && text[i + 1].isLetter()) {
                        val isChar = (i + 2 < text.length && text[i + 2] == '\'') ||
                                     (i + 3 < text.length && text[i + 1] == '\\' && text[i + 3] == '\'')
                        if (!isChar) {
                            i++
                            while (i < text.length && (text[i].isLetterOrDigit() || text[i] == '_')) { tick(); i++ }
                            emit(start, SyntaxStyle.KEYWORD)
                            continue
                        }
                    }
                    delimiter = c.toString()
                    i += delimiter.length; string()
                    var next = i
                    while (next < text.length && text[next].isWhitespace()) { checkCancelled(); next++ }
                    emit(start, if (kind in setOf("json", "yaml") && next < text.length && text[next] == ':') SyntaxStyle.KEY else SyntaxStyle.STRING)
                }
                c.isDigit() -> {
                    i++
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] in "._" ||
                            (text[i] in "+-" && text[i - 1] in "eEpP"))) { tick(); i++ }
                    emit(start, SyntaxStyle.NUMBER)
                }
                c.isLetter() || c == '_' || c == '$' -> {
                    i++
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] in "_$")) { tick(); i++ }
                    val word = text.subSequence(start, i).toString()
                    var next = i
                    while (next < text.length && text[next].isWhitespace()) { checkCancelled(); next++ }
                    if (kind == "yaml" && next < text.length && text[next] == ':') emit(start, SyntaxStyle.KEY)
                    else if (word in words) emit(start, SyntaxStyle.KEYWORD)
                }
                else -> { i++; if (c in "{}[]():,=+-*/!<>?&|%;.@#^~") emit(start, SyntaxStyle.OPERATOR) }
            }
        }
        return TokenizationResult(spans.toList(), TokenizerState(depth, delimiter))
    }
}

/** XML tags, attributes, entities, comments and CDATA; state crosses line and viewport boundaries. */
internal object XmlTokenizer : LanguageTokenizer {
    override fun tokenize(text: CharSequence, state: TokenizerState, checkCancelled: () -> Unit): TokenizationResult {
        val spans = mutableListOf<SyntaxSpan>()
        var tag = state.commentDepth // 0 = text, 1 = attributes, 2 = tag name still pending
        var delimiter = state.delimiter
        var i = 0
        var work = 0
        fun tick() { if (work++ % 256 == 0) checkCancelled() }
        fun at(value: String) = i + value.length <= text.length && value.indices.all { text[i + it] == value[it] }
        fun emit(start: Int, style: SyntaxStyle) { if (i > start) spans += SyntaxSpan(start, i, style) }
        fun until(end: String, style: SyntaxStyle, start: Int = i) {
            while (i < text.length) {
                tick()
                if (at(end)) { i += end.length; delimiter = ""; break }
                i++
            }
            emit(start, style)
        }
        while (i < text.length) {
            checkCancelled()
            val start = i
            when {
                delimiter.isNotEmpty() -> until(delimiter,
                    if (delimiter == "-->") SyntaxStyle.COMMENT else SyntaxStyle.STRING)
                at("<!--") -> { tag = 0; delimiter = "-->"; i += 4; until(delimiter, SyntaxStyle.COMMENT, start) }
                at("<![CDATA[") -> { tag = 0; delimiter = "]]>"; i += 9; until(delimiter, SyntaxStyle.STRING, start) }
                at("</") || at("<?") || at("<!") -> {
                    i += 2; tag = 2; emit(start, SyntaxStyle.OPERATOR)
                }
                text[i] == '<' -> { i++; tag = 2; emit(start, SyntaxStyle.OPERATOR) }
                tag != 0 && (at("/>") || at("?>")) -> {
                    i += 2; tag = 0; emit(start, SyntaxStyle.OPERATOR)
                }
                tag != 0 && text[i] == '>' -> { i++; tag = 0; emit(start, SyntaxStyle.OPERATOR) }
                tag != 0 && (text[i] == '"' || text[i] == '\'') -> {
                    delimiter = text[i].toString(); i++; until(delimiter, SyntaxStyle.STRING, start)
                }
                text[i] == '&' -> {
                    i++
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] in "#xX")) { tick(); i++ }
                    if (i < text.length && text[i] == ';') { i++; emit(start, SyntaxStyle.OPERATOR) }
                }
                tag != 0 && (text[i].isLetter() || text[i] == '_' || text[i] == ':') -> {
                    i++
                    while (i < text.length && (text[i].isLetterOrDigit() || text[i] in "_:-.")) { tick(); i++ }
                    emit(start, if (tag == 2) SyntaxStyle.KEYWORD else SyntaxStyle.KEY)
                    tag = 1
                }
                tag != 0 && text[i] in "=/" -> { i++; emit(start, SyntaxStyle.OPERATOR) }
                else -> i++
            }
        }
        return TokenizationResult(spans, TokenizerState(tag, delimiter))
    }
}

internal object MarkdownTokenizer : LanguageTokenizer {
    override fun tokenize(text: CharSequence, state: TokenizerState, checkCancelled: () -> Unit): TokenizationResult {
        checkCancelled()
        val value = text.toString().trimStart()
        val marker = when { value.startsWith("```") -> '`'; value.startsWith("~~~") -> '~'; else -> null }
        val fence = marker?.let { value.takeWhile { char -> char == it } }.orEmpty()
        val next = if (state.fence.isNotEmpty()) {
            if (fence.firstOrNull() == state.fence.first() && fence.length >= state.fence.length &&
                value.drop(fence.length).isBlank()) TokenizerState() else state
        } else if (fence.isNotEmpty()) TokenizerState(fence = fence) else state
        val style = when {
            state.fence.isNotEmpty() || fence.isNotEmpty() -> SyntaxStyle.STRING
            value.startsWith('#') -> SyntaxStyle.HEADING
            value.startsWith('>') -> SyntaxStyle.COMMENT
            else -> null
        }
        return TokenizationResult(if (style != null && text.isNotEmpty()) listOf(SyntaxSpan(0, text.length, style)) else emptyList(), next)
    }
}
