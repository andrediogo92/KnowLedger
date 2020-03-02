@file:UseSerializers(HashSerializer::class)
package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass

@Serializable
@SerialName("LedgerParams")
data class LedgerParams(
    val hasher: Hash,
    @SerialName("recalculationTime")
    val recalculationTime: Long = 1228800000,
    @SerialName("recalculationTrigger")
    val recalculationTrigger: Int = 2048,
    val blockParams: BlockParams = BlockParams()
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}