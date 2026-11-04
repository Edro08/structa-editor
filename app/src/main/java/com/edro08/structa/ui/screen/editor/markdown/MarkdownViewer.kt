package com.edro08.structa.ui.screen.editor.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edro08.structa.domain.editor.markdown.MarkdownNode
import com.edro08.structa.domain.editor.markdown.MarkdownSpan
import com.edro08.structa.domain.editor.markdown.TableAlignment
import com.edro08.structa.ui.theme.DarkOutlineVariant
import com.edro08.structa.ui.theme.DarkSurface
import com.edro08.structa.ui.theme.DarkSurfaceRaised
import com.edro08.structa.ui.theme.DarkTextPrimary
import com.edro08.structa.ui.theme.ScreenStyle
import com.edro08.structa.ui.theme.StructaCyan
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun MarkdownViewer(
    nodes: List<MarkdownNode>,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState()
) {
    val uriHandler = LocalUriHandler.current

    SelectionContainer(modifier = modifier) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                horizontal = ScreenStyle.pagePadding,
                vertical = 12.dp
            )
        ) {
            itemsIndexed(nodes) { _, node ->
                when (node) {
                    is MarkdownNode.Heading -> MarkdownHeadingItem(node) { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                    is MarkdownNode.Paragraph -> MarkdownParagraphItem(node) { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                    is MarkdownNode.CodeBlock -> MarkdownCodeBlockItem(node)
                    is MarkdownNode.BulletItem -> MarkdownBulletItem(node) { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                    is MarkdownNode.NumberedItem -> MarkdownNumberedItem(node) { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                    is MarkdownNode.Blockquote -> MarkdownBlockquoteItem(node) { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                    is MarkdownNode.ThematicBreak -> {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                    is MarkdownNode.Table -> MarkdownTableItem(node) { url ->
                        try { uriHandler.openUri(url) } catch (_: Exception) {}
                    }
                }
            }
        }
    }
}

@Composable
private fun MarkdownHeadingItem(
    heading: MarkdownNode.Heading,
    onLinkClick: (String) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val (style, defaultColor, topPad, bottomPad) = when (heading.level) {
        1 -> Quad(MaterialTheme.typography.headlineSmall, colors.onSurface, 18.dp, 8.dp)
        2 -> Quad(MaterialTheme.typography.titleLarge, colors.onSurface, 16.dp, 6.dp)
        3 -> Quad(MaterialTheme.typography.titleMedium, StructaLavender, 12.dp, 4.dp)
        else -> Quad(MaterialTheme.typography.titleSmall, colors.onSurface, 10.dp, 4.dp)
    }

    val annotated = buildMarkdownSpans(heading.spans, onLinkClick, defaultColor)
    Text(
        text = annotated,
        style = style,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = topPad, bottom = bottomPad)
    )
}

@Composable
private fun MarkdownParagraphItem(
    paragraph: MarkdownNode.Paragraph,
    onLinkClick: (String) -> Unit
) {
    val annotated = buildMarkdownSpans(paragraph.spans, onLinkClick)
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodyMedium,
        lineHeight = 22.sp,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f),
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun MarkdownBulletItem(
    item: MarkdownNode.BulletItem,
    onLinkClick: (String) -> Unit
) {
    val annotated = buildMarkdownSpans(item.spans, onLinkClick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (item.depth * 16).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "•",
            color = StructaPurple,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            modifier = Modifier.padding(end = 8.dp, top = 1.dp)
        )
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownNumberedItem(
    item: MarkdownNode.NumberedItem,
    onLinkClick: (String) -> Unit
) {
    val annotated = buildMarkdownSpans(item.spans, onLinkClick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = (item.depth * 16).dp, top = 2.dp, bottom = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "${item.number}.",
            color = StructaLavender,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownBlockquoteItem(
    quote: MarkdownNode.Blockquote,
    onLinkClick: (String) -> Unit
) {
    val annotated = buildMarkdownSpans(quote.spans, onLinkClick)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(IntrinsicSize.Min)
                .background(StructaPurple, RoundedCornerShape(2.dp))
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium,
            fontStyle = FontStyle.Italic,
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

private class TableCoordinates {
    var colStarts = intArrayOf()
    var colWidths = intArrayOf()
    var rowStarts = intArrayOf()
    var rowHeights = intArrayOf()
}

@Composable
private fun MarkdownTableItem(
    table: MarkdownNode.Table,
    onLinkClick: (String) -> Unit
) {
    if (table.headers.cells.isEmpty()) return

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {
        val density = LocalDensity.current
        val viewportWidthPx = with(density) { maxWidth.roundToPx() }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    1.dp,
                    DarkOutlineVariant.copy(alpha = 0.6f),
                    RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            color = DarkSurface
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                MarkdownTableLayout(
                    table = table,
                    viewportWidthPx = viewportWidthPx,
                    onLinkClick = onLinkClick
                )
            }
        }
    }
}

@Composable
private fun MarkdownTableLayout(
    table: MarkdownNode.Table,
    viewportWidthPx: Int,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val numColumns = table.headers.cells.size
    val numRows = 1 + table.rows.size
    val coords = remember { TableCoordinates() }

    Layout(
        content = {
            // 1. Headers
            table.headers.cells.forEachIndexed { colIdx, cell ->
                val alignment = table.alignments.getOrElse(colIdx) { TableAlignment.LEFT }
                val textAlign = when (alignment) {
                    TableAlignment.CENTER -> TextAlign.Center
                    TableAlignment.RIGHT -> TextAlign.End
                    TableAlignment.LEFT -> TextAlign.Start
                }
                Box(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    val headerSpans = buildMarkdownSpans(
                        cell.spans,
                        onLinkClick,
                        defaultColor = StructaLavender,
                        parentFontWeight = FontWeight.Bold
                    )
                    Text(
                        text = headerSpans,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = StructaLavender,
                        textAlign = textAlign
                    )
                }
            }

            // 2. Data Rows
            table.rows.forEach { row ->
                row.cells.forEachIndexed { colIdx, cell ->
                    val alignment = table.alignments.getOrElse(colIdx) { TableAlignment.LEFT }
                    val textAlign = when (alignment) {
                        TableAlignment.CENTER -> TextAlign.Center
                        TableAlignment.RIGHT -> TextAlign.End
                        TableAlignment.LEFT -> TextAlign.Start
                    }
                    Box(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        val cellSpans = buildMarkdownSpans(
                            cell.spans,
                            onLinkClick,
                            defaultColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f)
                        )
                        Text(
                            text = cellSpans,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f),
                            textAlign = textAlign
                        )
                    }
                }
            }
        },
        modifier = modifier.drawBehind {
            val cs = coords.colStarts
            val cw = coords.colWidths
            val rs = coords.rowStarts
            val rh = coords.rowHeights
            if (cs.isEmpty() || rs.isEmpty()) return@drawBehind

            // 1. Header background
            if (rh.isNotEmpty()) {
                drawRect(
                    color = DarkSurfaceRaised,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, rh[0].toFloat())
                )
            }

            // 2. Zebra background for alternate body rows (odd body rows)
            for (r in 1 until rs.size) {
                if (r % 2 == 0) {
                    drawRect(
                        color = DarkSurfaceRaised.copy(alpha = 0.35f),
                        topLeft = Offset(0f, rs[r].toFloat()),
                        size = Size(size.width, rh[r].toFloat())
                    )
                }
            }

            // 3. Horizontal grid dividers
            val dividerColor = DarkOutlineVariant.copy(alpha = 0.35f)
            val headerDividerColor = DarkOutlineVariant.copy(alpha = 0.75f)
            for (r in 1 until rs.size) {
                val y = rs[r].toFloat()
                val isHeaderDivider = (r == 1)
                drawLine(
                    color = if (isHeaderDivider) headerDividerColor else dividerColor,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = if (isHeaderDivider) 1.5.dp.toPx() else 0.8.dp.toPx()
                )
            }

            // 4. Vertical grid dividers between columns
            for (c in 1 until cs.size) {
                val x = cs[c].toFloat()
                drawLine(
                    color = dividerColor,
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
        }
    ) { measurables, _ ->
        if (measurables.size < numRows * numColumns) {
            return@Layout layout(0, 0) {}
        }

        val minColWidthPx = 100.dp.roundToPx()
        val maxColWidthPx = 320.dp.roundToPx()

        // Measure all cells with flexible constraints
        val cellConstraints = Constraints(
            minWidth = 0,
            maxWidth = maxColWidthPx,
            minHeight = 0,
            maxHeight = Constraints.Infinity
        )
        val placeables = measurables.map { it.measure(cellConstraints) }

        // Find max width for each column across ALL rows
        val colWidths = IntArray(numColumns)
        for (c in 0 until numColumns) {
            var maxW = minColWidthPx
            for (r in 0 until numRows) {
                val idx = r * numColumns + c
                maxW = maxOf(maxW, placeables[idx].width)
            }
            colWidths[c] = minOf(maxW, maxColWidthPx)
        }

        // Expand columns proportionally if total width is less than viewport
        val totalColWidth = colWidths.sum()
        if (totalColWidth < viewportWidthPx && numColumns > 0) {
            val extra = viewportWidthPx - totalColWidth
            val perCol = extra / numColumns
            val remainder = extra % numColumns
            for (c in 0 until numColumns) {
                colWidths[c] += perCol + (if (c < remainder) 1 else 0)
            }
        }

        // Find max height for each row across ALL columns
        val rowHeights = IntArray(numRows)
        for (r in 0 until numRows) {
            var maxH = 0
            for (c in 0 until numColumns) {
                val idx = r * numColumns + c
                maxH = maxOf(maxH, placeables[idx].height)
            }
            rowHeights[r] = maxH
        }

        val colStarts = IntArray(numColumns)
        var currX = 0
        for (c in 0 until numColumns) {
            colStarts[c] = currX
            currX += colWidths[c]
        }
        val totalWidth = currX

        val rowStarts = IntArray(numRows)
        var currY = 0
        for (r in 0 until numRows) {
            rowStarts[r] = currY
            currY += rowHeights[r]
        }
        val totalHeight = currY

        coords.colStarts = colStarts
        coords.colWidths = colWidths
        coords.rowStarts = rowStarts
        coords.rowHeights = rowHeights

        layout(totalWidth, totalHeight) {
            for (r in 0 until numRows) {
                for (c in 0 until numColumns) {
                    val idx = r * numColumns + c
                    val placeable = placeables[idx]
                    val cellW = colWidths[c]
                    val cellH = rowHeights[r]
                    val align = table.alignments.getOrElse(c) { TableAlignment.LEFT }

                    val x = when (align) {
                        TableAlignment.LEFT -> colStarts[c]
                        TableAlignment.CENTER -> colStarts[c] + (cellW - placeable.width) / 2
                        TableAlignment.RIGHT -> colStarts[c] + (cellW - placeable.width)
                    }
                    val y = rowStarts[r] + (cellH - placeable.height) / 2
                    placeable.placeRelative(x, y)
                }
            }
        }
    }
}

@Composable
private fun MarkdownCodeBlockItem(codeBlock: MarkdownNode.CodeBlock) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceRaised
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.5f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = codeBlock.language.ifEmpty { "CODE" }.uppercase(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StructaLavender
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            clipboard.setText(androidx.compose.ui.text.AnnotatedString(codeBlock.content))
                            copied = true
                            scope.launch {
                                delay(2000)
                                copied = false
                            }
                        }
                        .padding(horizontal = 6.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.Check else Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (copied) "Copiado" else "Copiar",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (copied) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = codeBlock.content,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = DarkTextPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(12.dp)
            )
        }
    }
}

@Composable
private fun buildMarkdownSpans(
    spans: List<MarkdownSpan>,
    onLinkClick: (String) -> Unit,
    defaultColor: Color? = null,
    parentFontWeight: FontWeight? = null,
    parentFontStyle: FontStyle? = null,
    parentColor: Color? = null
): AnnotatedString {
    val colors = MaterialTheme.colorScheme
    val fallbackColor = parentColor ?: defaultColor ?: colors.onSurface
    return buildAnnotatedString {
        appendSpans(spans, fallbackColor, parentFontWeight, parentFontStyle, onLinkClick)
    }
}

private fun AnnotatedString.Builder.appendSpans(
    spans: List<MarkdownSpan>,
    fallbackColor: Color,
    parentFontWeight: FontWeight?,
    parentFontStyle: FontStyle?,
    onLinkClick: (String) -> Unit
) {
    for (span in spans) {
        when (span) {
            is MarkdownSpan.Text -> {
                val style = SpanStyle(
                    color = fallbackColor,
                    fontWeight = parentFontWeight,
                    fontStyle = parentFontStyle
                )
                pushStyle(style)
                append(span.text)
                pop()
            }
            is MarkdownSpan.Bold -> {
                val weight = FontWeight.Bold
                if (span.children.isNotEmpty()) {
                    appendSpans(span.children, fallbackColor, weight, parentFontStyle, onLinkClick)
                } else {
                    pushStyle(SpanStyle(fontWeight = weight, fontStyle = parentFontStyle, color = fallbackColor))
                    append(span.text)
                    pop()
                }
            }
            is MarkdownSpan.Italic -> {
                val italic = FontStyle.Italic
                if (span.children.isNotEmpty()) {
                    appendSpans(span.children, fallbackColor, parentFontWeight, italic, onLinkClick)
                } else {
                    pushStyle(SpanStyle(fontStyle = italic, fontWeight = parentFontWeight, color = fallbackColor))
                    append(span.text)
                    pop()
                }
            }
            is MarkdownSpan.BoldItalic -> {
                val weight = FontWeight.Bold
                val italic = FontStyle.Italic
                if (span.children.isNotEmpty()) {
                    appendSpans(span.children, fallbackColor, weight, italic, onLinkClick)
                } else {
                    pushStyle(SpanStyle(fontWeight = weight, fontStyle = italic, color = fallbackColor))
                    append(span.text)
                    pop()
                }
            }
            is MarkdownSpan.Colored -> {
                val resolvedColor = parseColorString(span.color) ?: fallbackColor
                if (span.children.isNotEmpty()) {
                    appendSpans(span.children, resolvedColor, parentFontWeight, parentFontStyle, onLinkClick)
                } else {
                    pushStyle(SpanStyle(color = resolvedColor, fontWeight = parentFontWeight, fontStyle = parentFontStyle))
                    append(span.text)
                    pop()
                }
            }
            is MarkdownSpan.InlineCode -> {
                pushStyle(
                    SpanStyle(
                        fontFamily = FontFamily.Monospace,
                        background = DarkSurfaceRaised,
                        color = StructaCyan,
                        fontSize = 13.sp,
                        fontWeight = parentFontWeight ?: FontWeight.Normal
                    )
                )
                append(" ${span.text} ")
                pop()
            }
            is MarkdownSpan.Link -> {
                pushStyle(
                    SpanStyle(
                        color = StructaCyan,
                        textDecoration = TextDecoration.Underline,
                        fontWeight = FontWeight.Medium
                    )
                )
                if (span.children.isNotEmpty()) {
                    appendSpans(span.children, StructaCyan, FontWeight.Medium, parentFontStyle, onLinkClick)
                } else {
                    append(span.text)
                }
                pop()
            }
        }
    }
}

private fun parseColorString(colorStr: String): Color? {
    val clean = colorStr.trim().lowercase()
    return when (clean) {
        "orange" -> Color(0xFFF97316)
        "amber" -> Color(0xFFF59E0B)
        "yellow" -> Color(0xFFEAB308)
        "red" -> Color(0xFFEF4444)
        "green", "emerald" -> Color(0xFF10B981)
        "teal" -> Color(0xFF14B8A6)
        "cyan" -> Color(0xFF06B6D4)
        "blue" -> Color(0xFF3B82F6)
        "indigo" -> Color(0xFF6366F1)
        "violet", "purple" -> Color(0xFF8B5CF6)
        "fuchsia", "pink" -> Color(0xFFEC4899)
        "rose" -> Color(0xFFF43F5E)
        "gray", "grey", "slate" -> Color(0xFF94A3B8)
        "white" -> Color.White
        "black" -> Color.Black
        else -> {
            if (clean.startsWith("#")) {
                val hex = clean.removePrefix("#")
                try {
                    when (hex.length) {
                        3 -> {
                            val r = hex[0].toString().repeat(2).toInt(16)
                            val g = hex[1].toString().repeat(2).toInt(16)
                            val b = hex[2].toString().repeat(2).toInt(16)
                            Color(r, g, b)
                        }
                        6 -> Color(hex.toLong(16) or 0xFF000000L)
                        8 -> Color(hex.toLong(16))
                        else -> null
                    }
                } catch (_: Exception) {
                    null
                }
            } else null
        }
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
