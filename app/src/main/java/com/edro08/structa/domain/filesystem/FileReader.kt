package com.edro08.structa.domain.filesystem

import com.edro08.structa.domain.editor.encoding.DecodedContent
import com.edro08.structa.domain.editor.encoding.DocumentCharset

interface FileReader {
    suspend fun read(file: FileEntry): String

    suspend fun readWithCharset(file: FileEntry, explicitCharset: DocumentCharset? = null): DecodedContent {
        return DecodedContent(read(file), explicitCharset ?: DocumentCharset.DEFAULT)
    }
}
