package org.knowledger.ledger.core.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.base64.base64Encoded
import org.knowledger.ledger.core.flattenBytes
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.testing.core.random
import org.tinylog.kotlin.Logger

class TestHashingOperations {
    val hashSize = 16
    val minBound = 12
    val randomHashes = Array(random.randomInt(minBound) + 8) {
        Hash(random.randomByteArray(hashSize))
    }

    @Test
    fun `byte flattening`() {
        Logger.debug {
            """
                |
                |Expected: 
                | 
                |${randomHashes.joinToString(
                """
                    |
                    |
                """.trimMargin()
            ) {
                it.bytes.base64Encoded()
            }
            }""".trimMargin()
        }

        val expected = randomHashes.reduce { acc, bytes ->
            acc + bytes
        }
        val expectedShort = expected.bytes.sliceArray(0 until (minBound / 2 * hashSize))
        val test = flattenBytes(
            *randomHashes.map {
                it.bytes
            }.toTypedArray()
        )
        val test2 = flattenBytes(
            randomHashes
                .sliceArray(0 until (minBound / 2 - 1))
                .map {
                    it.bytes
                },
            randomHashes[minBound / 2 - 1].bytes
        )
        val test3 = flattenBytes(
            randomHashes.map {
                it.bytes
            }.toTypedArray()
        )
        assertThat(test.size).isEqualTo(expected.bytes.size)
        assertThat(test2.size).isEqualTo(expectedShort.size)
        assertThat(test3.size).isEqualTo(expected.bytes.size)
        Logger.debug {
            """
                |
                |Expected:
                |   ${expected.base64Encoded()}
                |Flatten via vararg byte arrays:
                |   ${test.base64Encoded()}
            """.trimMargin()
        }
        assertThat(test).containsExactly(*expected.bytes)
        Logger.debug {
            """
                |
                |Expected:
                |   ${expectedShort.base64Encoded()}
                |Flatten via collection + vararg byte arrays:
                |   ${test2.base64Encoded()}
            """.trimMargin()
        }
        assertThat(test2).containsExactly(*expectedShort)
        Logger.debug {
            """
                |
                |Expected:
                |   ${expected.base64Encoded()}
                |Flatten via direct AoA:
                |   ${test3.base64Encoded()}
            """.trimMargin()
        }
        assertThat(test3).containsExactly(*expected.bytes)
    }

}