package com.edro08.structa.domain.editor.draft

import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.encoding.DocumentCharset

/**
 * Snapshot of an unsaved draft document preserved for crash recovery.
 *
 * @property documentId The target document identifier (file path or URI).
 * @property content The uncommitted textual content at the moment of the snapshot.
 * @property timestamp Epoch millis when this draft was captured.
 * @property cursorAnchor Cursor selection anchor offset in UTF-16 code units.
 * @property cursorActive Active cursor head offset in UTF-16 code units.
 * @property scrollX Horizontal scroll position in pixels.
 * @property scrollY Vertical scroll position in pixels.
 * @property charset Active character encoding of the document.
 */
data class DocumentDraft(
    val documentId: DocumentId,
    val content: String,
    val timestamp: Long,
    val cursorAnchor: Int = 0,
    val cursorActive: Int = 0,
    val scrollX: Float = 0f,
    val scrollY: Float = 0f,
    val charset: DocumentCharset = DocumentCharset.DEFAULT
)
