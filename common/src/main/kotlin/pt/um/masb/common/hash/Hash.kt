package pt.um.masb.common.hash

import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.misc.printHexBinary
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
        get() = printHexBinary(bytes)

    // Only convert on truncation.
    val truncated: String
        get() = if (bytes.size > TRUNC) {
            printHexBinary(bytes.sliceArray(0..TRUNC))
        } else {
            printHexBinary(bytes)
        }

    val difficulty: Difficulty
        get() = Difficulty(BigInteger(bytes))


    companion object {
        const val TRUNC = 10

        val emptyHash: Hash = Hash(ByteArray(0))
    }
}