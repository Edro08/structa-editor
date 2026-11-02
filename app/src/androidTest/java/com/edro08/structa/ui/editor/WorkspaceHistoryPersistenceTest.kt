package com.edro08.structa.ui.editor

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.edro08.structa.data.settings.AndroidSettingsRepository
import com.edro08.structa.data.settings.AndroidWorkspaceHistoryRepository
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.filesystem.FileRef
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorkspaceHistoryPersistenceTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val settingsName = "test_settings_history"
    private val historyName = "test_workspace_history"

    @Before fun setUp() {
        context.getSharedPreferences(settingsName, Context.MODE_PRIVATE).edit().clear().commit()
        context.getSharedPreferences(historyName, Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun workspaceHistoryPersistsMetadataAcrossInstancesAndSyncsSettings() {
        val settings = AndroidSettingsRepository(context, settingsName)
        val history = AndroidWorkspaceHistoryRepository(context, settings, historyName)
        val first = DocumentId("content://example/tree/one")
        val second = DocumentId("content://example/tree/two")
        history.opened(first, 10L)
        history.opened(second, 20L)
        settings.setLastFolder(first)
        history.setFavorite(first, true)
        val restored = AndroidWorkspaceHistoryRepository(context, settings, historyName)
        assertEquals(listOf(second, first), restored.all().map { it.id })
        assertTrue(restored.all().single { it.id == first }.favorite)
        restored.remove(first)
        assertEquals(listOf(second), AndroidWorkspaceHistoryRepository(context, settings, historyName).all().map { it.id })
        assertEquals(first, settings.lastFolder())
    }

    @Test fun directWorkspaceProviderAndRootSurviveRecreation() {
        val settings = AndroidSettingsRepository(context, settingsName)
        val direct = FileRef(DocumentId("/storage/emulated/0/Documents/Structa"), "direct")
        settings.setLastFolderRef(direct)
        val history = AndroidWorkspaceHistoryRepository(context, settings, historyName)
        history.opened(direct.id, 2L)
        assertEquals(direct, AndroidSettingsRepository(context, settingsName).lastFolderRef())
        val restored = AndroidWorkspaceHistoryRepository(context, settings, historyName).all()
        assertEquals("direct", restored.single { it.id == direct.id }.providerId)
        assertEquals(direct.id.value, restored.single { it.id == direct.id }.path)
    }
}
