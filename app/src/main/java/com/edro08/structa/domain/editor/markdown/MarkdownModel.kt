package com.edro08.structa.domain.editor.markdown

/**
 * Spans that can appear inside a rich text block (Paragraph, ListItem, Blockquote, Heading).
 */
sealed interface MarkdownSpan {
    val text: String

    data class Text(override val text: String) : MarkdownSpan
    data class Bold(override val text: String) : MarkdownSpan
    data class Italic(override val text: String) : MarkdownSpan
    data class BoldItalic(override val text: String) : MarkdownSpan
    data class InlineCode(override val text: String) : MarkdownSpan
    data class Link(override val text: String, val url: String) : MarkdownSpan
    data class Colored(override val text: String, val color: String) : MarkdownSpan
}

/**
 * High-level block elements produced by the Markdown parser.
 */
sealed interface MarkdownNode {
    /** # H1 to ###### H6 with rich spans support */
    data class Heading(val level: Int, val spans: List<MarkdownSpan>) : MarkdownNode

    /** Standard text paragraph */
    data class Paragraph(val spans: List<MarkdownSpan>) : MarkdownNode

    /** Fenced code block ```lang ... ``` */
    data class CodeBlock(val language: String, val content: String) : MarkdownNode

    /** Unordered bullet item (- or * or +) */
    data class BulletItem(val depth: Int, val spans: List<MarkdownSpan>) : MarkdownNode

    /** Ordered item (1., 2., etc.) */
    data class NumberedItem(val depth: Int, val number: String, val spans: List<MarkdownSpan>) : MarkdownNode

    /** Quote block (> ...) */
    data class Blockquote(val spans: List<MarkdownSpan>) : MarkdownNode

    /** Horizontal divider (---, ***, ___) */
    object ThematicBreak : MarkdownNode
}
