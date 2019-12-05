@file:UseSerializers(PublicKeySerializer::class)
package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.data.PhysicalData
import java.security.PublicKey

@Serializable
internal data class TransactionImpl(
    // Agent's pub key.
    override val publicKey: PublicKey,
    override val data: PhysicalData
) : Transaction {
    override fun clone(): TransactionImpl =
        copy(
            publicKey = publicKey,
            data = data.clone()
        )

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override fun compareTo(other: Transaction): Int =
        data.compareTo(other.data)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transaction) return false

        if (publicKey != other.publicKey) return false
        if (data != other.data) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + data.hashCode()
        return result
    }


}
