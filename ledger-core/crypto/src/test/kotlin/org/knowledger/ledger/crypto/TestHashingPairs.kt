package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test
import org.knowledger.ledger.core.base.hash.toHexString
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.testing.core.applyHashInPairs
import org.knowledger.testing.core.random
import org.tinylog.kotlin.Logger


class TestHashingPairs {
    val crypter = DEFAULT_HASHER
    val hashSize = 16
    val minBound = 12
    val randomHashes = Array(random.randomInt(minBound) + 8) {
        Hash(random.randomByteArray(hashSize))
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
                |Test: ${test.toHexString()}
                |Expected: ${expected.toHexString()}
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
                |Test: ${test.toHexString()}
                |Expected: ${expected.toHexString()}
            """.trimMargin()
        }


        assertThat(expected.bytes).containsExactly(*test.bytes)
    }
}

