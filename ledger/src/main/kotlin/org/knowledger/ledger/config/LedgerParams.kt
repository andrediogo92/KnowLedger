@file:UseSerializers(HashSerializer::class)
package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass

@Serializable
data class LedgerParams(
    val hasher: Hash,
    val coinbaseParams: CoinbaseParams,
    val blockParams: BlockParams = BlockParams(),
    val recalculationTime: Long = 1228800000,
    val recalculationTrigger: Int = 2048
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}