package org.knowledger.ledger.crypto

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.data.ByteEncodable
import org.knowledger.ledger.crypto.serial.EncodedSignatureSerializer

@Serializable(with = EncodedSignatureSerializer::class)
data class EncodedSignature(override val bytes: ByteArray) : ByteEncodable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncodedSignature) return false

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}