package org.knowledger.ledger.crypto

import kotlinx.serialization.Serializable
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer

@Serializable(with = EncodedPublicKeySerializer::class)
data class EncodedPublicKey(val encoded: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncodedPublicKey) return false

        if (!encoded.contentEquals(other.encoded)) return false

        return true
    }

    override fun hashCode(): Int {
        return encoded.contentHashCode()
    }

}