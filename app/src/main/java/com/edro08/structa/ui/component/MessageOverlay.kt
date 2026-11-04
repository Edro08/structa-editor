package com.edro08.structa.ui.component

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.edro08.structa.R
import com.edro08.structa.ui.theme.StatusOrange
import com.edro08.structa.ui.theme.StatusRed
import kotlinx.coroutines.delay

@Composable
fun MessageOverlay(
    message: String,
    onDismiss: () -> Unit
) {
    MessageOverlay(message = message, modifier = Modifier, onDismiss = onDismiss)
}

@Composable
fun MessageOverlay(
    message: String,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    LaunchedEffect(message) {
        delay(3500)
        onDismiss()
    }

    val isError = remember(message) {
        message.contains("No se pudo", ignoreCase = true) ||
            message.contains("Error", ignoreCase = true) ||
            message.contains("falló", ignoreCase = true)
    }
    val isWarning = remember(message) {
        message.contains("Demasiadas", ignoreCase = true) ||
            message.contains("Espera", ignoreCase = true) ||
            message.contains("aviso", ignoreCase = true)
    }

    val icon: ImageVector = when {
        isError -> Icons.Filled.ErrorOutline
        isWarning -> Icons.Filled.WarningAmber
        else -> Icons.Filled.CheckCircle
    }

    val iconTint: Color = when {
        isError -> StatusRed
        isWarning -> StatusOrange
        else -> Color(0xFF44DB98) // Signature emerald green from Vaultk
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .imePadding(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 6.dp,
            tonalElevation = 3.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.common_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
