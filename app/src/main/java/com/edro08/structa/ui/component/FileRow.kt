package com.edro08.structa.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edro08.structa.R
import com.edro08.structa.domain.filesystem.FileEntry
import com.edro08.structa.ui.theme.ScreenStyle

@Composable
fun formatBytes(bytes: Long): String = when {
    bytes < 1024 -> stringResource(R.string.common_size_bytes, bytes)
    bytes < 1024 * 1024 -> stringResource(R.string.common_size_kilobytes, bytes / 1024)
    else -> stringResource(R.string.common_size_megabytes, bytes / (1024 * 1024))
}

@Composable
fun KotlinGradleLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // 1. Triángulo superior izquierdo (Cian a Violeta)
        val topPath = Path().apply {
            moveTo(0f, 0f)
            lineTo(w, 0f)
            lineTo(0f, h)
            close()
        }
        val topBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF00C7FF), Color(0xFF7F52FF)),
            start = Offset(0f, 0f),
            end = Offset(w, h * 0.5f)
        )
        drawPath(topPath, brush = topBrush)

        // 2. Trapecio / diagonal intermedia (Violeta a Magenta/Naranja)
        val middlePath = Path().apply {
            moveTo(w, 0f)
            lineTo(w, h * 0.5f)
            lineTo(w * 0.5f, h)
            lineTo(0f, h)
            close()
        }
        val middleBrush = Brush.linearGradient(
            colors = listOf(Color(0xFF7F52FF), Color(0xFFE4485D)),
            start = Offset(w * 0.5f, 0f),
            end = Offset(w * 0.25f, h)
        )
        drawPath(middlePath, brush = middleBrush)

        // 3. Triángulo inferior derecho (Naranja a Ámbar)
        val bottomPath = Path().apply {
            moveTo(w, h * 0.5f)
            lineTo(w, h)
            lineTo(w * 0.5f, h)
            close()
        }
        val bottomBrush = Brush.linearGradient(
            colors = listOf(Color(0xFFE4485D), Color(0xFFFF8900)),
            start = Offset(w * 0.75f, h * 0.5f),
            end = Offset(w, h)
        )
        drawPath(bottomPath, brush = bottomBrush)
    }
}

@Composable
fun FileEntryIcon(entry: FileEntry, modifier: Modifier = Modifier) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)
    val ext = entry.name.substringAfterLast('.', "").lowercase()

    if (entry.isDirectory) {
        // Carpeta clásica amarilla/dorada de gestor de archivos (Vaultk/ZArchiver)
        Box(
            modifier = modifier.size(42.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                modifier = Modifier.size(36.dp),
                tint = Color(0xFFF7C844)
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(42.dp)
                .clip(shape),
            contentAlignment = Alignment.Center
        ) {
            when {
                // PDF: Badge rojo con texto "PDF"
                ext == "pdf" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE53935)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "PDF",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                // ZIP / RAR / 7Z / TAR: Badge azul con texto "ZIP"
                ext in listOf("zip", "rar", "7z", "tar", "gz") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2563EB)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "ZIP",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                // APK / AAB: Badge violeta con texto "APK"
                ext in listOf("apk", "aab") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF7C3AED)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "APK",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                }
                // Kotlin (.kt, .kts, build.gradle.kts): Logo oficial de Kotlin
                entry.name.equals("build.gradle.kts", ignoreCase = true) ||
                entry.name.equals("build.gradle", ignoreCase = true) ||
                ext in listOf("kt", "kts") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        KotlinGradleLogo(modifier = Modifier.size(24.dp))
                    }
                }
                // Markdown (.md): Fondo pizarra con icono de documento en blanco
                ext == "md" -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF334155)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                            tint = Color.White
                        )
                    }
                }
                // Código (.java, .py, .js, .ts, .html, .css, .c, .cpp, .rs, .go, .sh)
                ext in listOf("java", "py", "js", "ts", "html", "css", "c", "cpp", "rs", "go", "sh") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Code,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFC4B5FD)
                        )
                    }
                }
                // Texto y config (.txt, .json, .xml, .yaml, .yml, .toml, .properties)
                ext in listOf("txt", "json", "xml", "yaml", "yml", "toml", "properties") -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF1E293B)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFF94A3B8)
                        )
                    }
                }
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(colors.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = colors.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FileRow(
    entry: FileEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    detail: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ScreenStyle.pagePadding, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FileEntryIcon(entry = entry)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.name,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Normal),
                color = colors.onSurface
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = detail ?: if (entry.isDirectory) stringResource(R.string.common_folder) else formatBytes(entry.sizeBytes),
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant
            )
        }
        actions()
    }
}
