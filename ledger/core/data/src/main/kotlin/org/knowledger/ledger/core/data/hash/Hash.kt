package org.knowledger.ledger.core.data.hash

import org.knowledger.ledger.core.data.ByteEncodable
import org.knowledger.ledger.core.data.Difficulty
import java.math.BigInteger

/**
 * Hash symbolizes a **unique identifier** for
 * a value structure instance which subsumes its value
 * into a digest of it.
 */
data class Hash(override val bytes: ByteArray) : ByteEncodable {
    operator fun plus(tx: Hash): Hash =
        Hash(bytes + tx.bytes)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Hash) return false

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }

    fun toDifficulty(): Difficulty =
        Difficulty(BigInteger(bytes))

    companion object {
        const val TRUNC = 10

        val emptyHash: Hash =
            Hash(ByteArray(0))
    }
}