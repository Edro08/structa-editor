package com.edro08.structa.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.edro08.structa.R
import com.edro08.structa.domain.settings.EditorFont
import com.edro08.structa.ui.component.StructaTopBar
import com.edro08.structa.ui.theme.ScreenStyle
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple
import com.edro08.structa.ui.theme.structaColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onBack: () -> Unit,
    onProvider: (String) -> Unit,
    onTheme: (Boolean) -> Unit,
    onFont: (EditorFont) -> Unit,
    onFontSize: (Int) -> Unit,
    onToggleAccessoryBar: (Boolean) -> Unit = {},
    directAuthorized: Boolean = false,
    onManagePermission: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme

    Scaffold(
        containerColor = colors.background,
        topBar = { StructaTopBar(title = { Text(stringResource(R.string.settings_title)) }, onBack = onBack) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(
                start = ScreenStyle.pagePadding,
                end = ScreenStyle.pagePadding,
                top = 6.dp,
                bottom = ScreenStyle.sectionGap
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // SECCIÓN 1: APARIENCIA
            item {
                SettingsCategoryHeader(title = "APARIENCIA")
                Spacer(Modifier.height(8.dp))

                PreferenceRow(
                    icon = Icons.Filled.Palette,
                    title = stringResource(R.string.settings_theme),
                    subtitle = if (state.darkTheme) "Modo Oscuro (Obsidian)" else "Modo Claro (Snow)",
                    onClick = { onTheme(!state.darkTheme) },
                    trailing = {
                        ThemeSegmentPill(
                            darkTheme = state.darkTheme,
                            onTheme = onTheme
                        )
                    }
                )
            }

            // DIVISOR
            item {
                HorizontalDivider(
                    color = colors.outlineVariant.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }

            // SECCIÓN 2: EDITOR DE TEXTO
            item {
                SettingsCategoryHeader(title = "EDITOR DE TEXTO")
                Spacer(Modifier.height(8.dp))

                // Fila Tipografía
                PreferenceRow(
                    icon = Icons.Filled.Code,
                    title = stringResource(R.string.settings_font),
                    subtitle = stringResource(when (state.editorFont) {
                        EditorFont.MONOSPACE -> R.string.settings_font_monospace
                        EditorFont.SANS_MONOSPACE -> R.string.settings_font_sans_monospace
                    }),
                    onClick = {
                        onFont(if (state.editorFont == EditorFont.MONOSPACE) EditorFont.SANS_MONOSPACE else EditorFont.MONOSPACE)
                    },
                    trailing = {
                        FontSegmentPill(
                            currentFont = state.editorFont,
                            onFont = onFont
                        )
                    }
                )

                Spacer(Modifier.height(10.dp))

                // Fila Tamaño de letra con Stepper
                PreferenceRow(
                    icon = Icons.Filled.TextFields,
                    title = "Tamaño de letra",
                    subtitle = "Escala de lectura en sp",
                    trailing = {
                        FontSizeStepper(
                            fontSize = state.editorFontSize,
                            onFontSize = onFontSize
                        )
                    }
                )

                Spacer(Modifier.height(10.dp))

                // Fila Barra de accesos rápidos / símbolos
                PreferenceRow(
                    icon = Icons.Filled.Keyboard,
                    title = "Barra de símbolos",
                    subtitle = "Fila de accesos rápidos sobre el teclado",
                    onClick = { onToggleAccessoryBar(!state.showAccessoryBar) },
                    trailing = {
                        Switch(
                            checked = state.showAccessoryBar,
                            onCheckedChange = onToggleAccessoryBar,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StructaPurple,
                                uncheckedThumbColor = colors.onSurfaceVariant,
                                uncheckedTrackColor = colors.surfaceVariant
                            )
                        )
                    }
                )
            }

            // DIVISOR
            item {
                HorizontalDivider(
                    color = colors.outlineVariant.copy(alpha = 0.2f),
                    thickness = 0.5.dp
                )
            }

            // SECCIÓN 3: ALMACENAMIENTO
            item {
                SettingsCategoryHeader(title = "ALMACENAMIENTO")
                Spacer(Modifier.height(8.dp))

                // Opción Acceso completo (Directo)
                StoragePreferenceItem(
                    icon = Icons.Filled.FolderOpen,
                    title = stringResource(R.string.settings_provider_direct),
                    badge = "Recomendado",
                    badgeColor = MaterialTheme.structaColors.statusSuccess,
                    description = stringResource(R.string.settings_direct_description),
                    selected = state.provider == "DIRECT",
                    onClick = { onProvider("DIRECT") },
                    statusContent = {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (directAuthorized) MaterialTheme.structaColors.statusSuccess else colors.outline)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = stringResource(if (directAuthorized) R.string.settings_direct_active else R.string.settings_direct_inactive),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (directAuthorized) colors.onSurface else colors.onSurfaceVariant
                                )
                            }
                            TextButton(
                                onClick = onManagePermission,
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Text(
                                    text = stringResource(if (directAuthorized) R.string.settings_direct_manage else R.string.settings_direct_grant),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = StructaLavender
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(8.dp))

                // Opción SAF
                StoragePreferenceItem(
                    icon = Icons.Filled.Security,
                    title = stringResource(R.string.settings_provider_saf),
                    badge = "Estándar",
                    badgeColor = colors.onSurfaceVariant,
                    description = stringResource(R.string.settings_saf_description),
                    selected = state.provider == "SAF",
                    onClick = { onProvider("SAF") }
                )
            }
        }
    }
}

@Composable
private fun SettingsCategoryHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        color = StructaLavender
    )
}

@Composable
private fun PreferenceRow(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme
    val rowModifier = if (onClick != null) {
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp)
    } else {
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 2.dp)
    }

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StructaLavender,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colors.onSurface
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurfaceVariant
                )
            }
        }

        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

@Composable
private fun ThemeSegmentPill(
    darkTheme: Boolean,
    onTheme: (Boolean) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val containerShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .clip(containerShape)
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.35f), containerShape)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val darkBg = if (darkTheme) StructaPurple else Color.Transparent
        val darkContent = if (darkTheme) Color.White else colors.onSurfaceVariant

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(darkBg)
                .clickable { onTheme(true) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.DarkMode, contentDescription = null, modifier = Modifier.size(13.dp), tint = darkContent)
                Text("Oscuro", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = darkContent)
            }
        }

        val lightBg = if (!darkTheme) StructaPurple else Color.Transparent
        val lightContent = if (!darkTheme) Color.White else colors.onSurfaceVariant

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(lightBg)
                .clickable { onTheme(false) }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(Icons.Filled.LightMode, contentDescription = null, modifier = Modifier.size(13.dp), tint = lightContent)
                Text("Claro", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = lightContent)
            }
        }
    }
}

@Composable
private fun FontSegmentPill(
    currentFont: EditorFont,
    onFont: (EditorFont) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val containerShape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .clip(containerShape)
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.35f), containerShape)
            .padding(2.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        val sansSelected = currentFont == EditorFont.SANS_MONOSPACE
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (sansSelected) colors.surfaceContainerHighest else Color.Transparent)
                .clickable { onFont(EditorFont.SANS_MONOSPACE) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Sans",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (sansSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (sansSelected) colors.onSurface else colors.onSurfaceVariant
            )
        }

        val monoSelected = currentFont == EditorFont.MONOSPACE
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (monoSelected) colors.surfaceContainerHighest else Color.Transparent)
                .clickable { onFont(EditorFont.MONOSPACE) }
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Mono",
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (monoSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (monoSelected) colors.onSurface else colors.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FontSizeStepper(
    fontSize: Int,
    onFontSize: (Int) -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(10.dp)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(colors.surfaceContainerHigh)
            .border(1.dp, colors.outlineVariant.copy(alpha = 0.35f), shape)
            .padding(horizontal = 3.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { if (fontSize > 10) onFontSize(fontSize - 1) },
            modifier = Modifier.size(28.dp),
            enabled = fontSize > 10
        ) {
            Text(
                text = "−",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (fontSize > 10) colors.onSurface else colors.outline
            )
        }

        Text(
            text = "$fontSize sp",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            color = StructaLavender,
            modifier = Modifier.width(50.dp),
            textAlign = TextAlign.Center
        )

        IconButton(
            onClick = { if (fontSize < 32) onFontSize(fontSize + 1) },
            modifier = Modifier.size(28.dp),
            enabled = fontSize < 32
        ) {
            Text(
                text = "+",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (fontSize < 32) colors.onSurface else colors.outline
            )
        }
    }
}

@Composable
private fun StoragePreferenceItem(
    icon: ImageVector,
    title: String,
    badge: String,
    badgeColor: Color,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    statusContent: (@Composable () -> Unit)? = null
) {
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 2.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colors.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = StructaLavender,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }

                // Radio Indicator
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, if (selected) StructaLavender else colors.outlineVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(StructaLavender)
                        )
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(top = 1.dp)
            )

            if (statusContent != null) {
                statusContent()
            }
        }
    }
}
