package com.edro08.structa.domain.editor.markdown

/**
 * Spans that can appear inside a rich text block (Paragraph, ListItem, Blockquote, Heading, TableCell).
 */
sealed interface MarkdownSpan {
    val text: String

    data class Text(override val text: String) : MarkdownSpan
    data class Bold(override val text: String, val children: List<MarkdownSpan> = emptyList()) : MarkdownSpan
    data class Italic(override val text: String, val children: List<MarkdownSpan> = emptyList()) : MarkdownSpan
    data class BoldItalic(override val text: String, val children: List<MarkdownSpan> = emptyList()) : MarkdownSpan
    data class InlineCode(override val text: String) : MarkdownSpan
    data class Link(override val text: String, val url: String, val children: List<MarkdownSpan> = emptyList()) : MarkdownSpan
    data class Colored(override val text: String, val color: String, val children: List<MarkdownSpan> = emptyList()) : MarkdownSpan
}

/**
 * Text alignment for Markdown table columns.
 */
enum class TableAlignment {
    LEFT,
    CENTER,
    RIGHT
}

/**
 * A single cell in a Markdown table with rich spans.
 */
data class TableCell(val spans: List<MarkdownSpan>)

/**
 * A row of cells in a Markdown table.
 */
data class TableRow(val cells: List<TableCell>)

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

    /** GFM Table with headers, alignments, and body rows */
    data class Table(
        val headers: TableRow,
        val alignments: List<TableAlignment>,
        val rows: List<TableRow>
    ) : MarkdownNode
}
