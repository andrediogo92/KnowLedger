package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.containsExactly
import org.junit.jupiter.api.Test
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.testing.core.applyHashInPairs
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.core.random
import org.tinylog.kotlin.Logger


class TestHashingPairs {
    private val hashers = defaultHasher
    private val minBound = 12
    private val randomHashes = Array(random.nextInt(minBound) + 8) { random.randomHash(hashers) }


    @Test
    fun `pair crypting balanced`() {
        val expectedFirst = hashers.applyHash(randomHashes[0] + randomHashes[1])
        val expectedSecond = hashers.applyHash(randomHashes[2] + randomHashes[3])
        val expectedThird = hashers.applyHash(randomHashes[4] + randomHashes[5])
        val expectedFourth = hashers.applyHash(randomHashes[6] + randomHashes[7])
        val expected = hashers.applyHash(
            hashers.applyHash(expectedFirst + expectedSecond) +
            hashers.applyHash(expectedThird + expectedFourth)
        )

        val test = applyHashInPairs(hashers, arrayOf(
            randomHashes[0], randomHashes[1], randomHashes[2], randomHashes[3],
            randomHashes[4], randomHashes[5], randomHashes[6], randomHashes[7],
        ))

        Logger.debug {
            """
                |
                |Test: ${test.base64Encoded()}
                |Expected: ${expected.base64Encoded()}
            """.trimMargin()
        }

        assertThat(expected.bytes).containsExactly(*test.bytes)

    }

    @Test
    fun `pair crypting unbalanced`() {
        val expectedFirst = hashers.applyHash(randomHashes[0] + randomHashes[1])
        val expectedSecond = hashers.applyHash(randomHashes[2] + randomHashes[3])
        val expectedThird = hashers.applyHash(randomHashes[4] + randomHashes[5])
        val expected = hashers.applyHash(
            hashers.applyHash(expectedFirst + expectedSecond) +
            hashers.applyHash(expectedThird + expectedThird)
        )

        val test = applyHashInPairs(hashers, arrayOf(
            randomHashes[0], randomHashes[1], randomHashes[2],
            randomHashes[3], randomHashes[4], randomHashes[5]
        ))

        Logger.info {
            """
                |
                |Test: ${test.base64Encoded()}
                |Expected: ${expected.base64Encoded()}
            """.trimMargin()
        }


        assertThat(expected.bytes).containsExactly(*test.bytes)
    }
}

