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
    val isDark: Boolean,
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
    val codeBlockBackground: Color,
    val codeBlockBorder: Color,
    val codeBlockHeader: Color,
    val codeBlockHeaderText: Color,
    val codeBlockText: Color,
    val inlineCodeBackground: Color,
    val inlineCodeText: Color,
    val inlineCodeBorder: Color,
    val link: Color,
    val sectionHeader: Color,
    val tableBackground: Color,
    val tableHeaderBackground: Color,
    val tableHeaderText: Color,
    val tableBorder: Color,
    val tableDivider: Color,
    val tableZebra: Color,
)

val LocalStructaCustomColors = staticCompositionLocalOf {
    StructaCustomColors(
        isDark = true,
        iconTileBackground = Color(0x264F46E5),
        iconTileBorder = Color(0x4D4F46E5),
        iconTileTint = StructaPurple,
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
        codeBlockBackground = DarkSurfaceRaised,
        codeBlockBorder = DarkOutlineVariant.copy(alpha = 0.5f),
        codeBlockHeader = DarkSurfaceContainerHighest.copy(alpha = 0.5f),
        codeBlockHeaderText = StructaPurple,
        codeBlockText = DarkTextPrimary,
        inlineCodeBackground = DarkSurfaceRaised,
        inlineCodeText = StructaCyan,
        inlineCodeBorder = DarkOutlineVariant.copy(alpha = 0.5f),
        link = StructaCyan,
        sectionHeader = StructaPurple,
        tableBackground = DarkSurface,
        tableHeaderBackground = DarkSurfaceRaised,
        tableHeaderText = StructaPurple,
        tableBorder = DarkOutlineVariant.copy(alpha = 0.6f),
        tableDivider = DarkOutlineVariant.copy(alpha = 0.35f),
        tableZebra = DarkSurfaceRaised.copy(alpha = 0.35f),
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
    primary = StructaIndigo,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEEF2FF),
    onPrimaryContainer = StructaIndigoDeep,
    secondary = StructaSecondary,
    onSecondary = Color.White,
    secondaryContainer = LightSurfaceContainerHigh,
    onSecondaryContainer = LightTextPrimary,
    tertiary = StructaBlue,
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
            isDark = true,
            iconTileBackground = Color(0x264F46E5),
            iconTileBorder = Color(0x4D4F46E5),
            iconTileTint = StructaPurple,
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
            codeBlockBackground = DarkSurfaceRaised,
            codeBlockBorder = DarkOutlineVariant.copy(alpha = 0.5f),
            codeBlockHeader = DarkSurfaceContainerHighest.copy(alpha = 0.5f),
            codeBlockHeaderText = StructaPurple,
            codeBlockText = DarkTextPrimary,
            inlineCodeBackground = DarkSurfaceRaised,
            inlineCodeText = StructaCyan,
            inlineCodeBorder = DarkOutlineVariant.copy(alpha = 0.5f),
            link = StructaCyan,
            sectionHeader = StructaPurple,
            tableBackground = DarkSurface,
            tableHeaderBackground = DarkSurfaceRaised,
            tableHeaderText = StructaPurple,
            tableBorder = DarkOutlineVariant.copy(alpha = 0.6f),
            tableDivider = DarkOutlineVariant.copy(alpha = 0.35f),
            tableZebra = DarkSurfaceRaised.copy(alpha = 0.35f),
        )
    } else {
        StructaCustomColors(
            isDark = false,
            iconTileBackground = Color(0xFFEEF2FF),
            iconTileBorder = Color(0xFFC7D2FE),
            iconTileTint = StructaIndigo,
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
            codeBlockBackground = Color(0xFFF1F5F9),
            codeBlockBorder = Color(0xFFCBD5E1),
            codeBlockHeader = Color(0xFFE2E8F0),
            codeBlockHeaderText = StructaIndigoDeep,
            codeBlockText = Color(0xFF0F172A),
            inlineCodeBackground = Color(0xFFE2E8F0),
            inlineCodeText = StructaIndigoDeep,
            inlineCodeBorder = Color(0xFFCBD5E1),
            link = StructaBlue,
            sectionHeader = StructaIndigoDeep,
            tableBackground = Color(0xFFFFFFFF),
            tableHeaderBackground = Color(0xFFF8FAFC),
            tableHeaderText = Color(0xFF1E293B),
            tableBorder = Color(0xFFE2E8F0),
            tableDivider = Color(0xFFE2E8F0),
            tableZebra = Color(0xFFF8FAFC),
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
