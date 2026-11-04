package com.edro08.structa.data.draft

import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.draft.DocumentDraft
import com.edro08.structa.domain.editor.draft.DraftRepository
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import java.io.File
import java.security.MessageDigest

/**
 * File-backed implementation of [DraftRepository] storing drafts as individual
 * atomic files within a dedicated directory.
 *
 * Each draft is written atomically via temporary file and rename to protect against
 * partial writes during unexpected process terminations or crashes.
 */
class FileDraftRepository(
    private val draftsDir: File
) : DraftRepository {

    init {
        if (!draftsDir.exists()) {
            draftsDir.mkdirs()
        }
    }

    override fun saveDraft(draft: DocumentDraft) {
        val key = keyFor(draft.documentId)
        val targetFile = File(draftsDir, "$key.draft")
        val tempFile = File(draftsDir, "$key.tmp")

        try {
            tempFile.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write("VERSION:1\n")
                writer.write("ID:${draft.documentId.value}\n")
                writer.write("TIME:${draft.timestamp}\n")
                writer.write("ANCHOR:${draft.cursorAnchor}\n")
                writer.write("ACTIVE:${draft.cursorActive}\n")
                writer.write("SCROLL:${draft.scrollX},${draft.scrollY}\n")
                writer.write("CHARSET:${draft.charset.name}\n")
                writer.write("---CONTENT---\n")
                writer.write(draft.content)
            }
            if (tempFile.exists()) {
                if (targetFile.exists()) {
                    targetFile.delete()
                }
                tempFile.renameTo(targetFile)
            }
        } catch (_: Exception) {
            tempFile.delete()
        }
    }

    override fun loadDraft(documentId: DocumentId): DocumentDraft? {
        val key = keyFor(documentId)
        val file = File(draftsDir, "$key.draft")
        return parseDraftFile(file)
    }

    override fun removeDraft(documentId: DocumentId) {
        val key = keyFor(documentId)
        val file = File(draftsDir, "$key.draft")
        val tmp = File(draftsDir, "$key.tmp")
        file.delete()
        tmp.delete()
    }

    override fun listDrafts(): List<DocumentDraft> {
        val files = draftsDir.listFiles { _, name -> name.endsWith(".draft") } ?: return emptyList()
        return files.mapNotNull { parseDraftFile(it) }
    }

    override fun clearAllDrafts() {
        val files = draftsDir.listFiles() ?: return
        for (f in files) {
            f.delete()
        }
    }

    private fun parseDraftFile(file: File): DocumentDraft? {
        if (!file.exists() || !file.isFile) return null
        return try {
            val text = file.readText(Charsets.UTF_8)
            val marker = "\n---CONTENT---\n"
            val markerIdx = text.indexOf(marker)
            if (markerIdx == -1) return null

            val header = text.substring(0, markerIdx)
            val content = text.substring(markerIdx + marker.length)

            var id = ""
            var time = 0L
            var anchor = 0
            var active = 0
            var scrollX = 0f
            var scrollY = 0f
            var charsetName = DocumentCharset.DEFAULT.name

            for (line in header.lineSequence()) {
                val colonIdx = line.indexOf(':')
                if (colonIdx == -1) continue
                val k = line.substring(0, colonIdx)
                val v = line.substring(colonIdx + 1)
                when (k) {
                    "ID" -> id = v
                    "TIME" -> time = v.toLongOrNull() ?: 0L
                    "ANCHOR" -> anchor = v.toIntOrNull() ?: 0
                    "ACTIVE" -> active = v.toIntOrNull() ?: 0
                    "SCROLL" -> {
                        val parts = v.split(',')
                        scrollX = parts.getOrNull(0)?.toFloatOrNull() ?: 0f
                        scrollY = parts.getOrNull(1)?.toFloatOrNull() ?: 0f
                    }
                    "CHARSET" -> charsetName = v
                }
            }

            if (id.isEmpty()) return null

            val resolvedCharset = DocumentCharset.entries.find { it.name == charsetName } ?: DocumentCharset.DEFAULT
            DocumentDraft(
                documentId = DocumentId(id),
                content = content,
                timestamp = time,
                cursorAnchor = anchor,
                cursorActive = active,
                scrollX = scrollX,
                scrollY = scrollY,
                charset = resolvedCharset
            )
        } catch (_: Exception) {
            null
        }
    }

    private fun keyFor(id: DocumentId): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(id.value.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
