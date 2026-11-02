package com.edro08.structa.ui.editor

import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import com.edro08.structa.domain.settings.EditorFont
import com.edro08.structa.ui.screen.settings.SettingsScreen
import com.edro08.structa.ui.screen.settings.SettingsUiState
import com.edro08.structa.ui.theme.StructaTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule val compose = createComposeRule()

    @Test fun cardsKeepThemeFontSizeAndControlsOperable() {
        var dark = true
        var font = EditorFont.MONOSPACE
        var size = 14
        compose.setContent {
            StructaTheme {
                SettingsScreen(SettingsUiState(), onBack = {},
                    onTheme = { dark = it }, onFont = { font = it }, onFontSize = { size = it })
            }
        }
        compose.onNodeWithText("Claro").performClick()
        compose.onNodeWithText("Sans monoespaciada").performClick()
        compose.onNode(SemanticsMatcher("slider") { it.config.contains(SemanticsActions.SetProgress) })
            .performSemanticsAction(SemanticsActions.SetProgress) { it(20f) }
        compose.runOnIdle {
            assertEquals(false, dark)
            assertEquals(EditorFont.SANS_MONOSPACE, font)
            assertEquals(20, size)
        }
    }
}
