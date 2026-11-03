package com.edro08.structa.ui.screen.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.edro08.structa.R
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import com.edro08.structa.ui.theme.DarkOutlineVariant
import com.edro08.structa.ui.theme.DarkSurface
import com.edro08.structa.ui.theme.DarkTextPrimary
import com.edro08.structa.ui.theme.DarkTextSecondary
import com.edro08.structa.ui.theme.StatusOrange
import com.edro08.structa.ui.theme.StructaCyan
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple

/**
 * Pure OLED dialog for choosing or auto-detecting document character encodings.
 */
@Composable
fun EncodingSelectionDialog(
    currentCharset: DocumentCharset,
    onSelect: (DocumentCharset) -> Unit,
    onAutoDetect: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .border(
                    1.dp,
                    DarkOutlineVariant.copy(alpha = 0.5f),
                    RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with icon badge, title and close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(StructaPurple.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = null,
                                tint = StructaLavender,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Codificación del archivo",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = DarkTextPrimary
                            )
                            Text(
                                text = "Selecciona el juego de caracteres para leer o guardar",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.common_cancel),
                            tint = DarkTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Scrollable list of encodings
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auto-detect option
                    AutoDetectOptionCard(
                        onClick = onAutoDetect
                    )

                    HorizontalDivider(
                        color = DarkOutlineVariant.copy(alpha = 0.3f),
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Manual Charsets
                    DocumentCharset.entries.forEach { charset ->
                        val isSelected = currentCharset == charset
                        EncodingOptionCard(
                            charset = charset,
                            isSelected = isSelected,
                            onClick = { onSelect(charset) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AutoDetectOptionCard(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                1.dp,
                StructaPurple.copy(alpha = 0.45f),
                RoundedCornerShape(14.dp)
            ),
        shape = RoundedCornerShape(14.dp),
        color = StructaPurple.copy(alpha = 0.08f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(StructaPurple.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = StructaLavender,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Detectar automáticamente",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = StructaLavender
                )
                Text(
                    text = "Analiza por BOM o secuencias UTF-8 (fallback: UTF-8)",
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
            }
        }
    }
}

@Composable
private fun EncodingOptionCard(
    charset: DocumentCharset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val (badgeText, badgeColor, description) = when (charset) {
        DocumentCharset.UTF_8 -> Triple("UTF-8", StructaLavender, "Estándar Unicode universal · Predeterminado")
        DocumentCharset.UTF_8_BOM -> Triple("BOM", StructaCyan, "UTF-8 con firma Byte Order Mark")
        DocumentCharset.UTF_16_LE -> Triple("16LE", Color(0xFF38BDF8), "Unicode Little Endian · Windows / .NET")
        DocumentCharset.UTF_16_BE -> Triple("16BE", Color(0xFF818CF8), "Unicode Big Endian · Java / Redes")
        DocumentCharset.WINDOWS_1252 -> Triple("ANSI", StatusOrange, "ANSI Windows Europa Occidental / CP1252")
        DocumentCharset.ISO_8859_1 -> Triple("ISO", Color(0xFFFBBF24), "Europa Occidental estándar ISO Latin 1")
        DocumentCharset.US_ASCII -> Triple("ASCII", DarkTextSecondary, "ASCII básico de 7 bits en inglés")
        DocumentCharset.IBM_437 -> Triple("DOS", Color(0xFFA78BFA), "Codepage clásico de MS-DOS / PC OEM")
        DocumentCharset.WINDOWS_1250 -> Triple("CE", Color(0xFF34D399), "Europa Central y del Este / CP1250")
    }

    val backgroundColor = if (isSelected) {
        StructaPurple.copy(alpha = 0.12f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.35f)
    }

    val borderColor = if (isSelected) {
        StructaPurple.copy(alpha = 0.65f)
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
    }

    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(badgeColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = badgeColor
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = charset.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = DarkTextPrimary
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = DarkTextSecondary
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(StructaPurple, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
