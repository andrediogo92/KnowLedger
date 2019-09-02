@file:UseSerializers(ByteArraySerializer::class)
package org.knowledger.ledger.core.hash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.misc.toHexString
import org.knowledger.ledger.core.serial.ByteArraySerializer
import java.math.BigInteger

/**
 * Hash symbolizes a **unique identifier** for
 * a value structure instance which subsumes its value
 * into a digest of it.
 */
@Serializable
@SerialName("hash")
data class Hash(val bytes: ByteArray) {
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

    val print: String
        get() = bytes.toHexString()

    val truncated: String
        get() = if (bytes.size > TRUNC) {
            bytes.sliceArray(0..TRUNC).toHexString()
        } else {
            bytes.toHexString()
        }

    val difficulty: Difficulty
        get() = Difficulty(BigInteger(bytes))


    companion object {
        const val TRUNC = 10

        val emptyHash: Hash = Hash(ByteArray(0))
    }
}