package com.edro08.structa.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.edro08.structa.R
import com.edro08.structa.ui.theme.ScreenStyle

enum class Screen { HOME, BROWSER, EDITOR, SETTINGS, SELECT_DIRECTORY }

@Composable
fun AppNavigation(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    val colors = MaterialTheme.colorScheme

    Column(
        Modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        HorizontalDivider(
            thickness = 0.5.dp,
            color = colors.outlineVariant.copy(alpha = 0.5f)
        )
        Row(
            Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(ScreenStyle.navigationHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(Screen.HOME, Screen.BROWSER, Screen.EDITOR, Screen.SETTINGS).forEach { screen ->
                val (icon, label) = when (screen) {
                    Screen.HOME -> Icons.Default.Home to R.string.nav_home
                    Screen.BROWSER -> Icons.Default.Folder to R.string.nav_browser
                    Screen.EDITOR -> Icons.Default.Code to R.string.nav_editor
                    Screen.SETTINGS -> Icons.Default.Settings to R.string.nav_settings
                    Screen.SELECT_DIRECTORY -> error("El selector no forma parte de la navegación inferior")
                }
                val selected = currentScreen == screen
                TextButton(
                    onClick = { onScreenSelected(screen) },
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(0.dp),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (selected) colors.primary else colors.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            Modifier
                                .width(60.dp)
                                .height(32.dp)
                                .background(
                                    if (selected) colors.primary.copy(alpha = 0.16f) else Color.Transparent,
                                    RoundedCornerShape(16.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(label),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }
    }
}
