@file:UseSerializers(ByteArraySerializer::class, PublicKeySerializer::class)

package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.serial.ByteArraySerializer
import org.knowledger.ledger.core.serial.PublicKeySerializer
import java.security.PublicKey

@Serializable
@SerialName("Transaction")
internal data class TransactionImpl(
    // Agent's pub key.
    override val publicKey: PublicKey,
    override val data: PhysicalData
) : Transaction {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)

    override fun compareTo(other: Transaction): Int =
        data.compareTo(other.data)
}
