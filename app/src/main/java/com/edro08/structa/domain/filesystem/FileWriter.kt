package com.edro08.structa.domain.filesystem

import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.encoding.DocumentCharset

interface FileWriter {
    suspend fun write(destination: DocumentId, content: String)

    suspend fun writeWithCharset(destination: DocumentId, content: String, charset: DocumentCharset) {
        write(destination, content)
    }
}
