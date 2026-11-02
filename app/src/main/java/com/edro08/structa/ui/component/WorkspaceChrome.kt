package com.edro08.structa.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edro08.structa.ui.theme.ScreenStyle
import com.edro08.structa.ui.theme.StructaIndigo
import com.edro08.structa.ui.theme.StructaLavender
import com.edro08.structa.ui.theme.StructaPurple
import com.edro08.structa.ui.theme.StructaViolet

@Composable
fun WorkspacePanel(modifier: Modifier = Modifier, compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(if (compact) ScreenStyle.tileRadius else ScreenStyle.panelRadius)
    val colors = MaterialTheme.colorScheme
    Column(modifier.clip(shape).background(Brush.linearGradient(
        listOf(colors.surfaceContainerLow, colors.surfaceContainerHigh)))
        .border(1.dp, colors.outlineVariant, shape)
        .padding(if (compact) 10.dp else 14.dp), content = content)
}

@Composable
fun WorkspaceBadge(modifier: Modifier = Modifier, label: String? = null) {
    val shape = RoundedCornerShape(10.dp)
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(shape)
            .background(StructaPurple.copy(alpha = 0.15f))
            .border(1.dp, StructaPurple.copy(alpha = 0.30f), shape),
        contentAlignment = Alignment.Center
    ) {
        if (label == null) {
            Icon(
                imageVector = Icons.Filled.Folder,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = StructaLavender
            )
        } else {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = StructaLavender
            )
        }
    }
}

@Composable
fun WorkspaceSectionTitle(icon: ImageVector, title: String, description: String? = null,
    action: (@Composable () -> Unit)? = null) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = StructaPurple, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        action?.invoke()
    }
    if (description != null) Text(description, style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2,
        overflow = TextOverflow.Ellipsis)
}

@Composable
fun WorkspaceActionChip(text: String, icon: ImageVector, enabled: Boolean = true, onClick: () -> Unit) {
    FilledTonalButton(onClick = onClick, enabled = enabled, shape = RoundedCornerShape(26.dp),
        contentPadding = PaddingValues(horizontal = 14.dp),
        colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(7.dp))
        Text(text, maxLines = 1)
    }
}

@Composable
fun WorkspaceSelection(label: String, selected: Boolean, enabled: Boolean = true, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().height(43.dp), verticalAlignment = Alignment.CenterVertically) {
        RadioButton(selected = selected, onClick = onClick, enabled = enabled, modifier = Modifier.size(40.dp),
            colors = RadioButtonDefaults.colors(selectedColor = MaterialTheme.colorScheme.primary))
        TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 10.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface)) {
            Text(label, modifier = Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun WorkspacePrimaryButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(24.dp)
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = shape,
        color = Color.Transparent,
        shadowElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(StructaViolet, StructaIndigo)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
