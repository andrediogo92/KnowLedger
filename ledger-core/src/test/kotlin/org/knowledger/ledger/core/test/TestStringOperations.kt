package org.knowledger.ledger.core.test

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.hashFromHexString

class TestStringOperations {
    val canonicHex: (ByteArray) -> String = {
        val sb = StringBuilder(it.size * 2)
        for (b in it) {
            sb.append(String.format("%02x", b).toUpperCase())
        }
        sb.toString()
    }

    val canonicParse: (String) -> ByteArray = {
        val data = ByteArray(it.length / 2)
        var i = 0
        while (i < it.length) {
            data[i / 2] = Integer.decode("0x" + it[i] + it[i + 1]).toByte()
            i += 2
        }
        data
    }

    @Test
    fun `hex string printing and parsing`() {
        repeat(200) {
            val testHash = Hash(randomByteArray(256))
            val expectedString = canonicHex(testHash.bytes)

            assertThat(testHash.print)
                .isEqualTo(expectedString)

            assertThat(testHash.print.hashFromHexString().bytes)
                .isEqualTo(canonicParse(expectedString))
        }
    }
}