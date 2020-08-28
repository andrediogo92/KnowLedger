@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.data.hash.Hash
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.storage.PhysicalData

@Serializable
data class ImmutableTransaction(
    override val hash: Hash,
    override val signature: EncodedSignature,
    override val publicKey: EncodedPublicKey,
    override val data: PhysicalData,
    override val approximateSize: Int = -1,
) : HashedTransaction {
    @OptIn(ExperimentalSerializationApi::class)
    override fun processTransaction(encoder: BinaryFormat): Boolean =
        verifySignature(encoder)
}

