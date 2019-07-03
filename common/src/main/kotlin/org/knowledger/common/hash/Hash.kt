package org.knowledger.common.hash

import org.knowledger.common.data.Difficulty
import org.knowledger.common.misc.hexString
import java.math.BigInteger

/**
 * Hash symbolizes a **unique identifier** for
 * a value structure instance which subsumes its value
 * into an index.
 */
inline class Hash(val bytes: ByteArray) {
    fun contentEquals(other: Hash): Boolean =
        bytes.contentEquals(other.bytes)

    fun contentHashCode(): Int =
        bytes.contentHashCode()

    operator fun plus(tx: Hash): Hash =
        Hash(bytes + tx.bytes)

    // Only convert on print.
    val print: String
        get() = bytes.hexString

    // Only convert on truncation.
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