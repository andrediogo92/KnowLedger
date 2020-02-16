package org.knowledger.ledger.crypto

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.data.ByteEncodable
import org.knowledger.ledger.crypto.serial.EncodedPrivateKeySerializer

@Serializable(with = EncodedPrivateKeySerializer::class)
data class EncodedPrivateKey(
    override val bytes: ByteArray
) : ByteEncodable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EncodedPrivateKey) return false

        if (!bytes.contentEquals(other.bytes)) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes.contentHashCode()
    }
}