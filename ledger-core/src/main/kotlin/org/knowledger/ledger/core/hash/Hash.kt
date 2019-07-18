package org.knowledger.ledger.core.hash

import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.misc.hexString
import java.math.BigInteger

/**
 * Hash symbolizes a **unique identifier** for
 * a value structure instance which subsumes its value
 * into a digest of it.
 */
inline class Hash(val bytes: ByteArray) {
    fun contentEquals(other: Hash): Boolean =
        bytes.contentEquals(other.bytes)

    fun contentHashCode(): Int =
        bytes.contentHashCode()

    operator fun plus(tx: Hash): Hash =
        Hash(bytes + tx.bytes)

    val print: String
        get() = bytes.hexString

    val truncated: String
        get() = if (bytes.size > TRUNC) {
            bytes.sliceArray(0..TRUNC).hexString
        } else {
            bytes.hexString
        }

    val difficulty: Difficulty
        get() = Difficulty(BigInteger(bytes))


    companion object {
        const val TRUNC = 10

        val emptyHash: Hash = Hash(ByteArray(0))
    }
}