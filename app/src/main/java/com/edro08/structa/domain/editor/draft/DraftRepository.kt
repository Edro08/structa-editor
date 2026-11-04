package com.edro08.structa.domain.editor.draft

import com.edro08.structa.domain.document.DocumentId

/**
 * Storage contract for persistent document drafts used in crash recovery.
 *
 * Implementations must ensure that writing drafts is atomic and does not
 * corrupt existing data in case of sudden process termination or power loss.
 */
interface DraftRepository {
    /**
     * Atomically stores or overwrites the draft for the specified document.
     */
    fun saveDraft(draft: DocumentDraft)

    /**
     * Loads the uncommitted draft associated with [documentId], or null if none exists.
     */
    fun loadDraft(documentId: DocumentId): DocumentDraft?

    /**
     * Deletes the draft for [documentId] when changes are saved to disk or discarded.
     */
    fun removeDraft(documentId: DocumentId)

    /**
     * Returns all currently stored drafts.
     */
    fun listDrafts(): List<DocumentDraft>

    /**
     * Purges all saved drafts from persistent storage.
     */
    fun clearAllDrafts()
}
