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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edro08.structa.domain.editor.markdown.MarkdownNode
import com.edro08.structa.domain.editor.markdown.MarkdownSpan
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = topPad, bottom = bottomPad)
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
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
            .clip(RoundedCornerShape(6.dp))
            .background(StructaPurple.copy(alpha = 0.08f))
            .border(
                1.dp,
                StructaPurple.copy(alpha = 0.18f),
                RoundedCornerShape(6.dp)
            )
            .padding(10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(20.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(StructaPurple)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodyMedium.copy(fontStyle = FontStyle.Italic),
            lineHeight = 21.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MarkdownCodeBlockItem(codeBlock: MarkdownNode.CodeBlock) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shape = RoundedCornerShape(10.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(shape)
            .background(DarkSurfaceRaised)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f), shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val langLabel = if (codeBlock.language.isNotBlank()) codeBlock.language.uppercase() else "CODE"
            Text(
                text = langLabel,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                color = StructaLavender
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable {
                        clipboard.setText(AnnotatedString(codeBlock.content))
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

@Composable
private fun buildMarkdownSpans(
    spans: List<MarkdownSpan>,
    onLinkClick: (String) -> Unit,
    defaultColor: Color? = null
): AnnotatedString {
    val colors = MaterialTheme.colorScheme
    val fallbackColor = defaultColor ?: colors.onSurface
    return buildAnnotatedString {
        for (span in spans) {
            when (span) {
                is MarkdownSpan.Text -> {
                    pushStyle(SpanStyle(color = fallbackColor))
                    append(span.text)
                    pop()
                }
                is MarkdownSpan.Bold -> {
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fallbackColor))
                    append(span.text)
                    pop()
                }
                is MarkdownSpan.Italic -> {
                    pushStyle(SpanStyle(fontStyle = FontStyle.Italic, color = fallbackColor))
                    append(span.text)
                    pop()
                }
                is MarkdownSpan.BoldItalic -> {
                    pushStyle(
                        SpanStyle(
                            fontWeight = FontWeight.Bold,
                            fontStyle = FontStyle.Italic,
                            color = fallbackColor
                        )
                    )
                    append(span.text)
                    pop()
                }
                is MarkdownSpan.Colored -> {
                    val resolvedColor = parseColorString(span.color) ?: fallbackColor
                    pushStyle(SpanStyle(color = resolvedColor))
                    append(span.text)
                    pop()
                }
                is MarkdownSpan.InlineCode -> {
                    pushStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = DarkSurfaceRaised,
                            color = StructaCyan,
                            fontSize = 13.sp
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
                    append(span.text)
                    pop()
                }
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
        "green" -> Color(0xFF10B981)
        "emerald" -> Color(0xFF10B981)
        "teal" -> Color(0xFF14B8A6)
        "cyan" -> Color(0xFF06B6D4)
        "blue" -> Color(0xFF3B82F6)
        "indigo" -> Color(0xFF6366F1)
        "violet", "purple" -> Color(0xFF8B5CF6)
        "fuchsia", "pink" -> Color(0xFFEC4899)
        "rose" -> Color(0xFFF43F5E)
        "gray", "grey" -> Color(0xFF94A3B8)
        "slate" -> Color(0xFF94A3B8)
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
