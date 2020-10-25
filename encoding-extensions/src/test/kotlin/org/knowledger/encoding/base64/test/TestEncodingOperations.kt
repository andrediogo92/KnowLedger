package org.knowledger.encoding.base64.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.knowledger.encoding.EncodedString
import org.knowledger.encoding.base16.hexDecoded
import org.knowledger.encoding.base16.hexDecodedToUTF8
import org.knowledger.encoding.base16.hexEncoded
import org.knowledger.encoding.base32.base32Decoded
import org.knowledger.encoding.base32.base32DecodedToUTF8
import org.knowledger.encoding.base32.base32Encoded
import org.knowledger.encoding.base64.base64Decoded
import org.knowledger.encoding.base64.base64DecodedToUTF8
import org.knowledger.encoding.base64.base64Encoded
import org.tinylog.kotlin.Logger

class TestEncodingOperations {
    val sampleString = "thisissampletext"
    val sampleBytes = byteArrayOf(
        0x74, 0x68, 0x69, 0x73, 0x69, 0x73, 0x73, 0x61,
        0x6d, 0x70, 0x6c, 0x65, 0x74, 0x65, 0x78, 0x74
    )

    private fun baseXStringAssertAndLog(
        expected: String,
        encoding: String.() -> EncodedString,
        decoding: EncodedString.() -> String,
    ) {
        val encoded = sampleString.encoding()
        val decoded = encoded.decoding()
        assertThat(encoded).isEqualTo(expected)
        assertThat(decoded).isEqualTo(sampleString)

        Logger.debug {
            """ 
                |
                | Input: $sampleString
                | Base64: $encoded
                | Decoded: $decoded
            """.trimMargin()
        }
    }

    private fun baseXBytesAssertAndLog(
        expected: String,
        encoding: ByteArray.() -> EncodedString,
        decoding: EncodedString.() -> ByteArray,
    ) {
        val encoded = sampleBytes.encoding()
        val decoded = encoded.decoding()
        assertThat(encoded).isEqualTo(expected)
        assertThat(decoded).isEqualTo(sampleBytes)

        Logger.debug {
            """ 
                |
                | Input: ${sampleBytes.decodeToString()}
                | Base64: $encoded
                | Decoded: ${decoded.decodeToString()}
            """.trimMargin()
        }
    }

    @Nested
    inner class Base64 {
        //URL-Safe base64 gets padding '=' trimmed.
        val expected = "dGhpc2lzc2FtcGxldGV4dA"

        @Test
        fun `base64 encoding decoding from string`() {
            baseXStringAssertAndLog(
                expected, String::base64Encoded, EncodedString::base64DecodedToUTF8
            )
        }

        @Test
        fun `base64 encoding decoding from UTF-8 bytes`() {
            baseXBytesAssertAndLog(
                expected, ByteArray::base64Encoded, EncodedString::base64Decoded
            )
        }
    }

    @Nested
    inner class Base16 {
        val expected = "74686973697373616D706C6574657874"

        @Test
        fun `base16 encoding decoding from string`() {
            baseXStringAssertAndLog(
                expected, String::hexEncoded, EncodedString::hexDecodedToUTF8
            )
        }

        @Test
        fun `base16 encoding decoding from UTF-8 bytes`() {
            baseXBytesAssertAndLog(
                expected, ByteArray::hexEncoded, EncodedString::hexDecoded
            )
        }
    }

    @Nested
    inner class Base32 {
        val expected = "EHK6ISR9EDPM2RBGDHIN8PBOEG======"

        @Test
        fun `base32 encoding decoding from string`() {
            baseXStringAssertAndLog(
                expected, String::base32Encoded, EncodedString::base32DecodedToUTF8
            )
        }

        @Test
        fun `base32 encoding decoding from UTF-8 bytes`() {
            baseXBytesAssertAndLog(
                expected, ByteArray::base32Encoded, EncodedString::base32Decoded
            )
        }
    }


}