package com.edro08.structa.domain.editor.markdown

/**
 * Pure Kotlin line-based Markdown parser.
 * Converts markdown text into structured [MarkdownNode] blocks without external dependencies.
 */
object MarkdownParser {

    private val HEADING_REGEX = Regex("""^(#{1,6})\s+(.+)$""")
    private val THEMATIC_BREAK_REGEX = Regex("""^\s*(\*{3,}|-{3,}|_{3,})\s*$""")
    private val BULLET_LIST_REGEX = Regex("""^(\s*)[-*+]\s+(.+)$""")
    private val NUMBERED_LIST_REGEX = Regex("""^(\s*)(\d+)[\.\)]\s+(.+)$""")
    private val BLOCKQUOTE_REGEX = Regex("""^\s*>\s?(.*)$""")

    // Inline regex:
    // Group 1: `code`
    // Group 2, 3: [link text](url)
    // Group 4, 5: <span style="color: ...">...</span>
    // Group 6, 7: <font color="...">...</font>
    // Group 8: <b>...</b>
    // Group 9: <strong>...</strong>
    // Group 10: <i>...</i>
    // Group 11: <em>...</em>
    // Group 12: <code>...</code>
    // Group 13: ***bold italic***
    // Group 14: ___bold italic___
    // Group 15: **bold**
    // Group 16: __bold__
    // Group 17: *italic*
    // Group 18: _italic_
    // Uncaptured: other HTML tags <...>
    private val INLINE_REGEX = Regex(
        """`([^`]+)`|""" +
        """\[([^\]]+)\]\(([^)]+)\)|""" +
        """<span\s+style=["']color:\s*([^"';]+)[^"']*["']>(.*?)</span>|""" +
        """<font\s+color=["']([^"']+)["']>(.*?)</font>|""" +
        """<b>(.*?)</b>|""" +
        """<strong>(.*?)</strong>|""" +
        """<i>(.*?)</i>|""" +
        """<em>(.*?)</em>|""" +
        """<code>(.*?)</code>|""" +
        """\*\*\*([^*]+)\*\*\*|""" +
        """___([^_]+)___|""" +
        """\*\*([^*]+)\*\*|""" +
        """__([^_]+)__|""" +
        """(?<!\*)\*([^*]+)\*(?!\*)|""" +
        """(?<!_)_([^_]+)_(?!_)|""" +
        """<[^>]+>""",
        setOf(RegexOption.IGNORE_CASE)
    )

    fun parse(text: CharSequence): List<MarkdownNode> {
        if (text.isEmpty()) return emptyList()

        val lines = text.split("\r\n", "\n", "\r")
        val nodes = mutableListOf<MarkdownNode>()

        var inCodeFence = false
        var codeLang = ""
        val codeLines = mutableListOf<String>()

        val paragraphBuffer = StringBuilder()

        fun flushParagraph() {
            if (paragraphBuffer.isNotEmpty()) {
                val pText = paragraphBuffer.toString().trim()
                if (pText.isNotEmpty()) {
                    nodes.add(MarkdownNode.Paragraph(parseInline(pText)))
                }
                paragraphBuffer.clear()
            }
        }

        for (line in lines) {
            val trimmed = line.trim()

            // 1. Fenced Code Blocks (```)
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) {
                if (inCodeFence) {
                    nodes.add(MarkdownNode.CodeBlock(codeLang, codeLines.joinToString("\n")))
                    codeLines.clear()
                    codeLang = ""
                    inCodeFence = false
                } else {
                    flushParagraph()
                    inCodeFence = true
                    codeLang = trimmed.substring(3).trim()
                    codeLines.clear()
                }
                continue
            }

            if (inCodeFence) {
                codeLines.add(line)
                continue
            }

            // 2. Empty / Blank line separates blocks
            if (trimmed.isEmpty()) {
                flushParagraph()
                continue
            }

            // 3. Thematic break (---, ***, ___)
            if (THEMATIC_BREAK_REGEX.matches(line)) {
                flushParagraph()
                nodes.add(MarkdownNode.ThematicBreak)
                continue
            }

            // 4. Headings (# H1 to ###### H6)
            val headingMatch = HEADING_REGEX.matchEntire(line)
            if (headingMatch != null) {
                flushParagraph()
                val level = headingMatch.groupValues[1].length
                val title = headingMatch.groupValues[2].trim()
                nodes.add(MarkdownNode.Heading(level, parseInline(title)))
                continue
            }

            // 5. Blockquote (> ...)
            val quoteMatch = BLOCKQUOTE_REGEX.matchEntire(line)
            if (quoteMatch != null) {
                flushParagraph()
                val content = quoteMatch.groupValues[1]
                nodes.add(MarkdownNode.Blockquote(parseInline(content)))
                continue
            }

            // 6. Unordered Bullet list (- item, * item, + item)
            val bulletMatch = BULLET_LIST_REGEX.matchEntire(line)
            if (bulletMatch != null) {
                flushParagraph()
                val indent = bulletMatch.groupValues[1].length
                val content = bulletMatch.groupValues[2].trim()
                nodes.add(MarkdownNode.BulletItem(depth = indent / 2, parseInline(content)))
                continue
            }

            // 7. Ordered Numbered list (1. item, 2. item)
            val numberedMatch = NUMBERED_LIST_REGEX.matchEntire(line)
            if (numberedMatch != null) {
                flushParagraph()
                val indent = numberedMatch.groupValues[1].length
                val number = numberedMatch.groupValues[2]
                val content = numberedMatch.groupValues[3].trim()
                nodes.add(MarkdownNode.NumberedItem(depth = indent / 2, number = number, parseInline(content)))
                continue
            }

            // 8. Normal text line -> append to paragraph buffer
            if (paragraphBuffer.isNotEmpty()) {
                paragraphBuffer.append(" ")
            }
            paragraphBuffer.append(line.trim())
        }

        if (inCodeFence) {
            nodes.add(MarkdownNode.CodeBlock(codeLang, codeLines.joinToString("\n")))
        }

        flushParagraph()

        return nodes
    }

    /**
     * Parses inline spans (plain text, bold, italic, bold+italic, colored, inline code, links) within a string.
     */
    fun parseInline(text: String): List<MarkdownSpan> {
        if (text.isEmpty()) return emptyList()

        val spans = mutableListOf<MarkdownSpan>()
        var lastIndex = 0

        for (match in INLINE_REGEX.findAll(text)) {
            val start = match.range.first
            if (start > lastIndex) {
                spans.add(MarkdownSpan.Text(text.substring(lastIndex, start)))
            }

            val code = match.groupValues[1]
            val linkText = match.groupValues[2]
            val linkUrl = match.groupValues[3]
            val spanColor = match.groupValues[4]
            val spanText = match.groupValues[5]
            val fontColor = match.groupValues[6]
            val fontText = match.groupValues[7]
            val bText = match.groupValues[8]
            val strongText = match.groupValues[9]
            val iText = match.groupValues[10]
            val emText = match.groupValues[11]
            val codeHtmlText = match.groupValues[12]
            val boldItalic1 = match.groupValues[13]
            val boldItalic2 = match.groupValues[14]
            val bold1 = match.groupValues[15]
            val bold2 = match.groupValues[16]
            val italic1 = match.groupValues[17]
            val italic2 = match.groupValues[18]

            when {
                code.isNotEmpty() -> spans.add(MarkdownSpan.InlineCode(code))
                linkText.isNotEmpty() -> spans.add(MarkdownSpan.Link(linkText, linkUrl))
                spanColor.isNotEmpty() -> spans.add(MarkdownSpan.Colored(spanText, spanColor))
                fontColor.isNotEmpty() -> spans.add(MarkdownSpan.Colored(fontText, fontColor))
                bText.isNotEmpty() -> spans.add(MarkdownSpan.Bold(bText))
                strongText.isNotEmpty() -> spans.add(MarkdownSpan.Bold(strongText))
                iText.isNotEmpty() -> spans.add(MarkdownSpan.Italic(iText))
                emText.isNotEmpty() -> spans.add(MarkdownSpan.Italic(emText))
                codeHtmlText.isNotEmpty() -> spans.add(MarkdownSpan.InlineCode(codeHtmlText))
                boldItalic1.isNotEmpty() -> spans.add(MarkdownSpan.BoldItalic(boldItalic1))
                boldItalic2.isNotEmpty() -> spans.add(MarkdownSpan.BoldItalic(boldItalic2))
                bold1.isNotEmpty() -> spans.add(MarkdownSpan.Bold(bold1))
                bold2.isNotEmpty() -> spans.add(MarkdownSpan.Bold(bold2))
                italic1.isNotEmpty() -> spans.add(MarkdownSpan.Italic(italic1))
                italic2.isNotEmpty() -> spans.add(MarkdownSpan.Italic(italic2))
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < text.length) {
            spans.add(MarkdownSpan.Text(text.substring(lastIndex)))
        }

        return spans
    }
}
