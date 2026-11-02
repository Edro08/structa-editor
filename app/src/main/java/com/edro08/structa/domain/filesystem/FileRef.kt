package com.edro08.structa.domain.filesystem

import com.edro08.structa.domain.document.DocumentId

/** Provider plus opaque identifier. Direct filesystem is the primary access method. */
data class FileRef(val id: DocumentId, val fileSystem: String = "direct") {
    companion object {
        fun of(id: DocumentId): FileRef = FileRef(id, "direct")
    }
}
