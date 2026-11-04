package com.edro08.structa.domain.editor.encoding

import java.nio.ByteBuffer
import java.nio.charset.CharacterCodingException
import java.nio.charset.CodingErrorAction
import java.nio.charset.StandardCharsets

/**
 * Result of decoding raw file bytes.
 */
data class DecodedContent(
    val text: String,
    val charset: DocumentCharset
)

/**
 * Pure Kotlin charset detector and transcoding utility.
 * Inspects BOMs, validates UTF-8 byte sequences, and falls back gracefully.
 */
object CharsetDetector {

    private val UTF8_BOM = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
    private val UTF16_LE_BOM = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
    private val UTF16_BE_BOM = byteArrayOf(0xFE.toByte(), 0xFF.toByte())

    /**
     * Detects the character encoding of a raw byte buffer.
     */
    fun detect(bytes: ByteArray): DocumentCharset {
        if (bytes.isEmpty()) return DocumentCharset.DEFAULT

        // 1. Check for explicit Byte Order Marks (BOM)
        if (hasBom(bytes, UTF8_BOM)) return DocumentCharset.UTF_8_BOM
        if (hasBom(bytes, UTF16_LE_BOM)) return DocumentCharset.UTF_16_LE
        if (hasBom(bytes, UTF16_BE_BOM)) return DocumentCharset.UTF_16_BE

        // 2. Validate strict UTF-8
        if (isValidUtf8(bytes)) {
            return DocumentCharset.UTF_8
        }

        // 3. Check for Windows-1252 ANSI bytes (0x80 to 0x9F)
        for (b in bytes) {
            val unsigned = b.toInt() and 0xFF
            if (unsigned in 0x80..0x9F) {
                return DocumentCharset.WINDOWS_1252
            }
        }

        // 4. Default ANSI fallback
        return DocumentCharset.WINDOWS_1252
    }

    /**
     * Decodes the raw bytes into a String using an explicit or auto-detected charset.
     */
    fun decode(bytes: ByteArray, explicitCharset: DocumentCharset? = null): DecodedContent {
        if (bytes.isEmpty()) {
            return DecodedContent("", explicitCharset ?: DocumentCharset.DEFAULT)
        }

        val charset = explicitCharset ?: detect(bytes)

        val text = when (charset) {
            DocumentCharset.UTF_8_BOM -> {
                val data = if (hasBom(bytes, UTF8_BOM)) bytes.copyOfRange(3, bytes.size) else bytes
                String(data, StandardCharsets.UTF_8)
            }
            DocumentCharset.UTF_16_LE -> {
                val data = if (hasBom(bytes, UTF16_LE_BOM)) bytes.copyOfRange(2, bytes.size) else bytes
                String(data, StandardCharsets.UTF_16LE)
            }
            DocumentCharset.UTF_16_BE -> {
                val data = if (hasBom(bytes, UTF16_BE_BOM)) bytes.copyOfRange(2, bytes.size) else bytes
                String(data, StandardCharsets.UTF_16BE)
            }
            else -> {
                try {
                    String(bytes, charset.javaCharset)
                } catch (_: Exception) {
                    String(bytes, StandardCharsets.UTF_8)
                }
            }
        }

        // Clean any leading BOM character that might have escaped into the string
        val cleanedText = if (text.startsWith('\uFEFF')) text.substring(1) else text

        return DecodedContent(cleanedText, charset)
    }

    /**
     * Encodes a String into bytes according to the specified charset, prepending BOM if required.
     */
    fun encode(text: String, charset: DocumentCharset): ByteArray {
        val bodyBytes = try {
            text.toByteArray(charset.javaCharset)
        } catch (_: Exception) {
            text.toByteArray(StandardCharsets.UTF_8)
        }

        return when (charset) {
            DocumentCharset.UTF_8_BOM -> UTF8_BOM + bodyBytes
            DocumentCharset.UTF_16_LE -> UTF16_LE_BOM + bodyBytes
            DocumentCharset.UTF_16_BE -> UTF16_BE_BOM + bodyBytes
            else -> bodyBytes
        }
    }

    private fun hasBom(bytes: ByteArray, bom: ByteArray): Boolean {
        if (bytes.size < bom.size) return false
        for (i in bom.indices) {
            if (bytes[i] != bom[i]) return false
        }
        return true
    }

    private fun isValidUtf8(bytes: ByteArray): Boolean {
        val decoder = StandardCharsets.UTF_8.newDecoder()
            .onMalformedInput(CodingErrorAction.REPORT)
            .onUnmappableCharacter(CodingErrorAction.REPORT)
        return try {
            decoder.decode(ByteBuffer.wrap(bytes))
            true
        } catch (_: CharacterCodingException) {
            false
        }
    }
}
