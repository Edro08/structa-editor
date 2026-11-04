package com.edro08.structa.ui.editor.draft

import androidx.compose.ui.text.input.TextFieldValue
import com.edro08.structa.application.document.OpenDocument
import com.edro08.structa.application.document.SaveDocumentCopy
import com.edro08.structa.application.editor.ContentFormatDetector
import com.edro08.structa.application.editor.FormatJsonDocument
import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.draft.DocumentDraft
import com.edro08.structa.domain.editor.draft.DraftRepository
import com.edro08.structa.domain.filesystem.FileEntry
import com.edro08.structa.domain.filesystem.FileReader
import com.edro08.structa.domain.filesystem.FileWriter
import com.edro08.structa.ui.screen.editor.EditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorDraftRecoveryTest {
    private val dispatcher = StandardTestDispatcher()
    private val file = FileEntry(DocumentId("/test/sample.txt"), "sample.txt", 15, false)
    private val file2 = FileEntry(DocumentId("/test/sample2.txt"), "sample2.txt", 20, false)
    private val disk = mutableMapOf(file.id to "Original content", file2.id to "Second content")

    private val reader = object : FileReader {
        override suspend fun read(file: FileEntry): String = disk.getValue(file.id)
    }
    private val writer = object : FileWriter {
        override suspend fun write(destination: DocumentId, content: String) { disk[destination] = content }
    }

    private class InMemoryDraftRepository : DraftRepository {
        val drafts = mutableMapOf<DocumentId, DocumentDraft>()
        override fun saveDraft(draft: DocumentDraft) { drafts[draft.documentId] = draft }
        override fun loadDraft(documentId: DocumentId): DocumentDraft? = drafts[documentId]
        override fun removeDraft(documentId: DocumentId) { drafts.remove(documentId) }
        override fun listDrafts(): List<DocumentDraft> = drafts.values.toList()
        override fun clearAllDrafts() { drafts.clear() }
    }

    private val draftRepo = InMemoryDraftRepository()

    private fun createViewModel(): EditorViewModel = EditorViewModel(
        openDocument = OpenDocument(reader, ContentFormatDetector()),
        formatDocument = FormatJsonDocument(),
        saveCopy = SaveDocumentCopy(writer),
        draftRepository = draftRepo
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun documentWithExistingDraftShowsRecoveryBanner() = runTest(dispatcher) {
        draftRepo.saveDraft(
            DocumentDraft(
                documentId = file.id,
                content = "Modified content before crash",
                timestamp = System.currentTimeMillis(),
                cursorAnchor = 5,
                cursorActive = 5,
                scrollX = 0f,
                scrollY = 120f
            )
        )

        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }

        // Initial content on screen is what was on disk
        assertEquals("Original content", model.state.value.value.text)
        // Recovery banner should be present
        val recovered = model.state.value.recoveredDraft
        assertNotNull(recovered)
        assertEquals("Modified content before crash", recovered!!.content)
        assertEquals(5, recovered.cursorAnchor)
        assertEquals(120f, recovered.scrollY, 0.001f)
    }

    @Test
    fun restoreDraftAppliesContentAndMarksDirty() = runTest(dispatcher) {
        draftRepo.saveDraft(
            DocumentDraft(
                documentId = file.id,
                content = "Restored draft text",
                timestamp = System.currentTimeMillis(),
                cursorAnchor = 8,
                cursorActive = 8
            )
        )

        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }

        model.restoreDraft()

        assertEquals("Restored draft text", model.state.value.value.text)
        assertTrue(model.state.value.dirty)
        assertNull(model.state.value.recoveredDraft)
        assertEquals(8, model.state.value.value.selection.start)
    }

    @Test
    fun discardRecoveredDraftRemovesDraftFromRepoAndClearsBanner() = runTest(dispatcher) {
        draftRepo.saveDraft(
            DocumentDraft(
                documentId = file.id,
                content = "Changes user wants to drop",
                timestamp = System.currentTimeMillis()
            )
        )

        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }

        model.discardRecoveredDraft()

        assertEquals("Original content", model.state.value.value.text)
        assertFalse(model.state.value.dirty)
        assertNull(model.state.value.recoveredDraft)
        assertNull(draftRepo.loadDraft(file.id))
    }

    @Test
    fun draftIdenticalToDiskContentIsAutomaticallyCleanedUp() = runTest(dispatcher) {
        draftRepo.saveDraft(
            DocumentDraft(
                documentId = file.id,
                content = "Original content",
                timestamp = System.currentTimeMillis()
            )
        )

        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }

        assertNull(model.state.value.recoveredDraft)
        assertNull(draftRepo.loadDraft(file.id))
    }

    @Test
    fun editingSchedulesDraftAutoSave() = runTest(dispatcher) {
        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }

        model.toggleEditable()
        model.edit(TextFieldValue("Original content + new edits"))
        assertTrue(model.state.value.dirty)

        advanceTimeBy(700)
        runCurrent()

        val savedDraft = draftRepo.loadDraft(file.id)
        assertNotNull(savedDraft)
        assertEquals("Original content + new edits", savedDraft!!.content)
    }

    @Test
    fun savingDocumentRemovesDraft() = runTest(dispatcher) {
        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }

        model.toggleEditable()
        model.edit(TextFieldValue("New text before save"))
        advanceTimeBy(700)
        runCurrent()
        assertNotNull(draftRepo.loadDraft(file.id))

        model.save()
        runCurrent()

        assertFalse(model.state.value.dirty)
        assertNull(draftRepo.loadDraft(file.id))
        assertEquals("New text before save", disk[file.id])
    }

    @Test
    fun flushAllDraftsSavesAllDirtyDocumentsImmediately() = runTest(dispatcher) {
        val model = createViewModel()
        model.open(file)
        model.state.first { it.entry?.id == file.id && !it.loading }
        model.toggleEditable()
        model.edit(TextFieldValue("File 1 dirty"))

        model.open(file2)
        model.state.first { it.entry?.id == file2.id && !it.loading }
        model.toggleEditable()
        model.edit(TextFieldValue("File 2 dirty"))

        // Don't wait for debounce - immediately flush
        model.flushAllDrafts()

        val draft1 = draftRepo.loadDraft(file.id)
        val draft2 = draftRepo.loadDraft(file2.id)
        assertNotNull(draft1)
        assertNotNull(draft2)
        assertEquals("File 1 dirty", draft1!!.content)
        assertEquals("File 2 dirty", draft2!!.content)
    }
}
