package com.edro08.structa.domain.editor.encoding

import org.junit.Assert.*
import org.junit.Test
import java.nio.charset.StandardCharsets

class CharsetDetectorTest {

    @Test
    fun detect_plainUtf8Text() {
        val bytes = "Hola mundo, bienvenidos a Structa!".toByteArray(StandardCharsets.UTF_8)
        val charset = CharsetDetector.detect(bytes)
        assertEquals(DocumentCharset.UTF_8, charset)
    }

    @Test
    fun detect_utf8WithBom() {
        val bom = byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte())
        val textBytes = "Texto con BOM".toByteArray(StandardCharsets.UTF_8)
        val charset = CharsetDetector.detect(bom + textBytes)
        assertEquals(DocumentCharset.UTF_8_BOM, charset)
    }

    @Test
    fun detect_utf16LeWithBom() {
        val bom = byteArrayOf(0xFF.toByte(), 0xFE.toByte())
        val textBytes = "UTF-16 LE Content".toByteArray(StandardCharsets.UTF_16LE)
        val charset = CharsetDetector.detect(bom + textBytes)
        assertEquals(DocumentCharset.UTF_16_LE, charset)
    }

    @Test
    fun detect_utf16BeWithBom() {
        val bom = byteArrayOf(0xFE.toByte(), 0xFF.toByte())
        val textBytes = "UTF-16 BE Content".toByteArray(StandardCharsets.UTF_16BE)
        val charset = CharsetDetector.detect(bom + textBytes)
        assertEquals(DocumentCharset.UTF_16_BE, charset)
    }

    @Test
    fun detect_windows1252_whenInvalidUtf8WithAnsiChars() {
        // 0x80 is the Euro symbol € in Windows-1252, which is an invalid standalone byte in UTF-8
        val bytes = byteArrayOf('P'.code.toByte(), 'r'.code.toByte(), 'e'.code.toByte(), 'c'.code.toByte(), 'i'.code.toByte(), 'o'.code.toByte(), ':'.code.toByte(), ' '.code.toByte(), 0x80.toByte(), '1'.code.toByte(), '0'.code.toByte())
        val charset = CharsetDetector.detect(bytes)
        assertEquals(DocumentCharset.WINDOWS_1252, charset)
    }

    @Test
    fun decode_stripsBomCleanly() {
        val raw = "Hola mundo"
        val encodedWithBom = CharsetDetector.encode(raw, DocumentCharset.UTF_8_BOM)
        val decoded = CharsetDetector.decode(encodedWithBom)

        assertEquals(DocumentCharset.UTF_8_BOM, decoded.charset)
        assertEquals(raw, decoded.text)
        assertFalse(decoded.text.startsWith('\uFEFF'))
    }

    @Test
    fun decode_utf16Le_roundtrip() {
        val raw = "Prueba de UTF-16 LE"
        val encoded = CharsetDetector.encode(raw, DocumentCharset.UTF_16_LE)
        val decoded = CharsetDetector.decode(encoded)

        assertEquals(DocumentCharset.UTF_16_LE, decoded.charset)
        assertEquals(raw, decoded.text)
    }

    @Test
    fun decode_windows1252_explicit() {
        val raw = "Configuración rápida"
        val encoded = raw.toByteArray(java.nio.charset.Charset.forName("windows-1252"))
        val decoded = CharsetDetector.decode(encoded, DocumentCharset.WINDOWS_1252)

        assertEquals(DocumentCharset.WINDOWS_1252, decoded.charset)
        assertEquals(raw, decoded.text)
    }

    @Test
    fun encode_prependsCorrectBomBytes() {
        val text = "ABC"
        val utf8Bom = CharsetDetector.encode(text, DocumentCharset.UTF_8_BOM)
        assertEquals(0xEF.toByte(), utf8Bom[0])
        assertEquals(0xBB.toByte(), utf8Bom[1])
        assertEquals(0xBF.toByte(), utf8Bom[2])

        val utf16Le = CharsetDetector.encode(text, DocumentCharset.UTF_16_LE)
        assertEquals(0xFF.toByte(), utf16Le[0])
        assertEquals(0xFE.toByte(), utf16Le[1])

        val utf16Be = CharsetDetector.encode(text, DocumentCharset.UTF_16_BE)
        assertEquals(0xFE.toByte(), utf16Be[0])
        assertEquals(0xFF.toByte(), utf16Be[1])
    }

    @Test
    fun detect_emptyBytes_returnsDefault() {
        assertEquals(DocumentCharset.DEFAULT, CharsetDetector.detect(byteArrayOf()))
    }
}
