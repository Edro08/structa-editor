package com.edro08.structa.domain.editor.encoding

import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Supported character encodings in Structa.
 * Fully pure Kotlin domain without Android framework dependencies.
 */
enum class DocumentCharset(
    val id: String,
    val displayName: String,
    val badge: String,
    val charsetName: String,
    val hasBom: Boolean = false
) {
    UTF_8("utf-8", "UTF-8", "UTF-8", "UTF-8"),
    UTF_8_BOM("utf-8-bom", "UTF-8 con BOM", "BOM", "UTF-8", hasBom = true),
    UTF_16_LE("utf-16le", "UTF-16 LE", "16LE", "UTF-16LE", hasBom = true),
    UTF_16_BE("utf-16be", "UTF-16 BE", "16BE", "UTF-16BE", hasBom = true),
    WINDOWS_1252("windows-1252", "ANSI (Windows-1252)", "ANSI", "windows-1252"),
    ISO_8859_1("iso-8859-1", "ISO-8859-1 (Latin 1)", "ISO", "ISO-8859-1"),
    US_ASCII("us-ascii", "ASCII (7-bit)", "ASCII", "US-ASCII"),
    IBM_437("ibm-437", "IBM437 (MS-DOS OEM)", "DOS", "IBM437"),
    WINDOWS_1250("windows-1250", "Windows-1250 (CE)", "CE", "windows-1250");

    val javaCharset: Charset by lazy {
        try {
            Charset.forName(charsetName)
        } catch (_: Exception) {
            StandardCharsets.UTF_8
        }
    }

    companion object {
        val DEFAULT = UTF_8

        fun fromId(id: String?): DocumentCharset {
            if (id.isNullOrBlank()) return DEFAULT
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: DEFAULT
        }

        fun fromCharsetName(name: String?): DocumentCharset {
            if (name.isNullOrBlank()) return DEFAULT
            return entries.firstOrNull { it.charsetName.equals(name, ignoreCase = true) } ?: DEFAULT
        }
    }
}
