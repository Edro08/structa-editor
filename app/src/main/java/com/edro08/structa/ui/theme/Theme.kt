package com.edro08.structa.ui.theme

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat

@Immutable
data class StructaCustomColors(
    val iconTileBackground: Color,
    val iconTileBorder: Color,
    val iconTileTint: Color,
    val textMuted: Color,
    val statusSuccess: Color,
    val statusWarning: Color,
    val statusError: Color,
    val codeKeyword: Color,
    val codeFunction: Color,
    val codeString: Color,
    val codeNumber: Color,
    val codeComment: Color,
    val codeOperator: Color,
)

val LocalStructaCustomColors = staticCompositionLocalOf {
    StructaCustomColors(
        iconTileBackground = Color(0x266366F1),
        iconTileBorder = Color(0x4D6366F1),
        iconTileTint = StructaLavender,
        textMuted = DarkTextMuted,
        statusSuccess = StatusGreen,
        statusWarning = StatusOrange,
        statusError = StatusRed,
        codeKeyword = CodeKeywordDark,
        codeFunction = CodeFunctionDark,
        codeString = CodeStringDark,
        codeNumber = CodeNumberDark,
        codeComment = CodeCommentDark,
        codeOperator = CodeOperatorDark,
    )
}

val MaterialTheme.structaCustomColors: StructaCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStructaCustomColors.current

val MaterialTheme.structaColors: StructaCustomColors
    @Composable
    @ReadOnlyComposable
    get() = LocalStructaCustomColors.current

private val DarkColors = darkColorScheme(
    primary = StructaPurple,
    onPrimary = Color.White,
    primaryContainer = Color(0x336366F1),
    onPrimaryContainer = StructaLavender,
    secondary = StructaCyan,
    onSecondary = Color.White,
    secondaryContainer = DarkSurfaceContainerHigh,
    onSecondaryContainer = DarkTextPrimary,
    tertiary = StructaFolder,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceRaised,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = StatusRed,
)

private val LightColors = lightColorScheme(
    primary = StructaPrimaryDark,
    onPrimary = Color.White,
    primaryContainer = LightSurfaceContainerHighest,
    onPrimaryContainer = LightTextPrimary,
    secondary = StructaSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceContainerHigh,
    onSecondaryContainer = LightTextPrimary,
    tertiary = StructaCyan,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceRaised,
    onSurfaceVariant = LightTextSecondary,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    error = StatusRed,
)

@Composable
fun StructaTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val customColors = if (darkTheme) {
        StructaCustomColors(
            iconTileBackground = DarkSurfaceRaised,
            iconTileBorder = DarkOutlineVariant,
            iconTileTint = DarkTextPrimary,
            textMuted = DarkTextMuted,
            statusSuccess = StatusGreen,
            statusWarning = StatusOrange,
            statusError = StatusRed,
            codeKeyword = CodeKeywordDark,
            codeFunction = CodeFunctionDark,
            codeString = CodeStringDark,
            codeNumber = CodeNumberDark,
            codeComment = CodeCommentDark,
            codeOperator = CodeOperatorDark,
        )
    } else {
        StructaCustomColors(
            iconTileBackground = LightSurfaceRaised,
            iconTileBorder = LightOutlineVariant,
            iconTileTint = LightTextPrimary,
            textMuted = LightTextMuted,
            statusSuccess = StatusGreen,
            statusWarning = StatusOrange,
            statusError = StatusRed,
            codeKeyword = CodeKeywordLight,
            codeFunction = CodeFunctionLight,
            codeString = CodeStringLight,
            codeNumber = CodeNumberLight,
            codeComment = CodeCommentLight,
            codeOperator = CodeOperatorLight,
        )
    }

    CompositionLocalProvider(LocalStructaCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColors else LightColors,
            typography = Typography,
            content = content
        )
    }
}

@Composable
fun StructaSystemBars(activity: ComponentActivity, darkTheme: Boolean) {
    SideEffect {
        activity.window.statusBarColor = (if (darkTheme) DarkBackground else LightBackground).toArgb()
        activity.window.navigationBarColor = (if (darkTheme) DarkBackground else LightBackground).toArgb()
        WindowCompat.getInsetsController(activity.window, activity.window.decorView).apply {
            isAppearanceLightStatusBars = !darkTheme
            isAppearanceLightNavigationBars = !darkTheme
        }
    }
}
