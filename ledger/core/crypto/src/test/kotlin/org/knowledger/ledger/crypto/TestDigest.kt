package org.knowledger.ledger.crypto

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isNotEqualTo
import org.junit.jupiter.api.Test
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.crypto.digest.classDigest
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.testing.core.defaultHasher
import org.knowledger.testing.ledger.RandomData
import org.tinylog.kotlin.Logger

class TestDigest {
    @Test
    fun `digests are identical`() {
        val random = RandomData::class
        val hashers = Hashers.Blake2b256Hasher::class
        val first = random.classDigest(defaultHasher)
        val second = random.classDigest(defaultHasher)
        val ofHashers = hashers.classDigest(defaultHasher)

        Logger.debug {
            """
                | Hash: ${first.base64Encoded()}
                | Matcher: ${second.base64Encoded()}
                | Hasher: ${ofHashers.base64Encoded()}
            """.trimMargin()
        }

        assertThat(first.bytes).containsExactly(*second.bytes)
        assertThat(ofHashers).isNotEqualTo(first)
    }
}