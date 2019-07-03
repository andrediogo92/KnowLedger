package org.knowledger.common.test

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.common.hash.Hash
import org.knowledger.common.misc.flattenBytes
import org.knowledger.common.misc.hexString
import org.tinylog.kotlin.Logger

class TestHashingOperations {
    val hashSize = 16
    val minBound = 12
    val randomHashes = Array(randomInt(minBound) + 8) {
        Hash(randomByteArray(hashSize))
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
                it.bytes.hexString
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
                |   ${expected.print}
                |Flatten via vararg byte arrays:
                |   ${test.hexString}
            """.trimMargin()
        }
        assertThat(test).containsExactly(*expected.bytes)
        Logger.debug {
            """
                |
                |Expected:
                |   ${expectedShort.hexString}
                |Flatten via collection + vararg byte arrays:
                |   ${test2.hexString}
            """.trimMargin()
        }
        assertThat(test2).containsExactly(*expectedShort)
        Logger.debug {
            """
                |
                |Expected:
                |   ${expected.print}
                |Flatten via direct AoA:
                |   ${test3.hexString}
            """.trimMargin()
        }
        assertThat(test3).containsExactly(*expected.bytes)
    }

    @Test
    fun `pair crypting balanced`() {
        val expected = crypter.applyHash(
            crypter.applyHash(
                crypter.applyHash(
                    randomHashes[0] + randomHashes[1]
                ) + crypter.applyHash(
                    randomHashes[2] + randomHashes[3]
                )
            ) + crypter.applyHash(
                crypter.applyHash(
                    randomHashes[4] + randomHashes[5]
                ) + crypter.applyHash(
                    randomHashes[6] + randomHashes[7]
                )
            )
        )

        val test = applyHashInPairs(
            crypter,
            arrayOf(
                randomHashes[0],
                randomHashes[1],
                randomHashes[2],
                randomHashes[3],
                randomHashes[4],
                randomHashes[5],
                randomHashes[6],
                randomHashes[7]
            )
        )

        Logger.debug {
            """
                |
                |Test: ${test.print}
                |Expected: ${expected.print}
            """.trimMargin()
        }

        assertThat(expected.bytes).containsExactly(*test.bytes)

    }

    @Test
    fun `pair crypting unbalanced`() {
        val expected = crypter.applyHash(
            crypter.applyHash(
                crypter.applyHash(
                    randomHashes[0] + randomHashes[1]
                ) + crypter.applyHash(
                    randomHashes[2] + randomHashes[3]
                )
            ) + crypter.applyHash(
                crypter.applyHash(
                    randomHashes[4] + randomHashes[5]
                ) + crypter.applyHash(
                    randomHashes[4] + randomHashes[5]
                )
            )
        )

        val test = applyHashInPairs(
            crypter,
            arrayOf(
                randomHashes[0],
                randomHashes[1],
                randomHashes[2],
                randomHashes[3],
                randomHashes[4],
                randomHashes[5]
            )
        )

        Logger.info {
            """
                |
                |Test: ${test.print}
                |Expected: ${expected.print}
            """.trimMargin()
        }


        assertThat(expected.bytes).containsExactly(*test.bytes)
    }
}