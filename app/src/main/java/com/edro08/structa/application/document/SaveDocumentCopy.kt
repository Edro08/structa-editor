package com.edro08.structa.application.document

import com.edro08.structa.domain.document.DocumentId
import com.edro08.structa.domain.editor.encoding.DocumentCharset
import com.edro08.structa.domain.filesystem.FileWriter

class SaveDocumentCopy(private val writer: FileWriter) {
    suspend operator fun invoke(
        destination: DocumentId,
        content: String,
        charset: DocumentCharset = DocumentCharset.DEFAULT
    ) = writer.writeWithCharset(destination, content, charset)
}
