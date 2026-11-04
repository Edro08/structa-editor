package com.edro08.structa.data.draft

import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.draft.DocumentDraft
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FileDraftRepositoryTest {

    private lateinit var tempDir: File
    private lateinit var repository: FileDraftRepository

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("structa_draft_test").toFile()
        repository = FileDraftRepository(tempDir)
    }

    @After
    fun tearDown() {
        tempDir.deleteRecursively()
    }

    @Test
    fun saveAndLoadDraft_roundTripsAllFieldsAccurately() {
        val docId = DocumentId("/storage/emulated/0/Download/main.kt")
        val content = "fun main() {\n    println(\"Hello, Structa! 🚀\")\n}\n"
        val draft = DocumentDraft(
            documentId = docId,
            content = content,
            timestamp = 1700000000123L,
            cursorAnchor = 15,
            cursorActive = 20,
            scrollX = 42.5f,
            scrollY = 120.0f,
            charset = DocumentCharset.UTF_8
        )

        repository.saveDraft(draft)
        val loaded = repository.loadDraft(docId)

        assertNotNull(loaded)
        assertEquals(docId, loaded!!.documentId)
        assertEquals(content, loaded.content)
        assertEquals(1700000000123L, loaded.timestamp)
        assertEquals(15, loaded.cursorAnchor)
        assertEquals(20, loaded.cursorActive)
        assertEquals(42.5f, loaded.scrollX, 0.01f)
        assertEquals(120.0f, loaded.scrollY, 0.01f)
        assertEquals(DocumentCharset.UTF_8, loaded.charset)
    }

    @Test
    fun overwriteDraft_updatesContentAtomically() {
        val docId = DocumentId("/storage/emulated/0/notes.txt")
        val draft1 = DocumentDraft(docId, "Version 1", 1000L)
        val draft2 = DocumentDraft(docId, "Version 2 with more lines\nand details", 2000L)

        repository.saveDraft(draft1)
        repository.saveDraft(draft2)

        val loaded = repository.loadDraft(docId)
        assertNotNull(loaded)
        assertEquals("Version 2 with more lines\nand details", loaded!!.content)
        assertEquals(2000L, loaded.timestamp)
    }

    @Test
    fun removeDraft_deletesSavedDraft() {
        val docId = DocumentId("/storage/emulated/0/project/README.md")
        val draft = DocumentDraft(docId, "# Draft notes", 1000L)

        repository.saveDraft(draft)
        assertNotNull(repository.loadDraft(docId))

        repository.removeDraft(docId)
        assertNull(repository.loadDraft(docId))
    }

    @Test
    fun listDrafts_returnsAllStoredDrafts() {
        val doc1 = DocumentDraft(DocumentId("/storage/doc1.txt"), "Doc 1", 100L)
        val doc2 = DocumentDraft(DocumentId("/storage/doc2.txt"), "Doc 2", 200L)
        val doc3 = DocumentDraft(DocumentId("/storage/doc3.txt"), "Doc 3", 300L)

        repository.saveDraft(doc1)
        repository.saveDraft(doc2)
        repository.saveDraft(doc3)

        val drafts = repository.listDrafts()
        assertEquals(3, drafts.size)
        val ids = drafts.map { it.documentId.value }.toSet()
        assertTrue(ids.contains("/storage/doc1.txt"))
        assertTrue(ids.contains("/storage/doc2.txt"))
        assertTrue(ids.contains("/storage/doc3.txt"))
    }

    @Test
    fun clearAllDrafts_removesEverything() {
        repository.saveDraft(DocumentDraft(DocumentId("/a.txt"), "A", 1L))
        repository.saveDraft(DocumentDraft(DocumentId("/b.txt"), "B", 2L))

        assertEquals(2, repository.listDrafts().size)

        repository.clearAllDrafts()
        assertEquals(0, repository.listDrafts().size)
    }

    @Test
    fun corruptedDraftFile_returnsNullGracefully() {
        val corruptFile = File(tempDir, "fake_corrupt.draft")
        corruptFile.writeText("Corrupted content without proper markers")

        val drafts = repository.listDrafts()
        assertEquals(0, drafts.size)
    }

    @Test
    fun emptyContentDraft_handlesEmptyStringsGracefully() {
        val docId = DocumentId("/storage/empty.txt")
        val draft = DocumentDraft(docId, "", 500L)

        repository.saveDraft(draft)
        val loaded = repository.loadDraft(docId)

        assertNotNull(loaded)
        assertEquals("", loaded!!.content)
    }
}
