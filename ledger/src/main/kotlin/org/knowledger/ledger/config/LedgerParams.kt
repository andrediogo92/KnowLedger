package org.knowledger.ledger.config

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass

@Serializable
@SerialName("LedgerParams")
data class LedgerParams(
    val crypter: Hash,
    @SerialName("recalculationTime")
    val recalcTime: Long = 1228800000,
    @SerialName("recalculationTrigger")
    val recalcTrigger: Long = 2048,
    val blockParams: BlockParams = BlockParams()
) : HashSerializable, ServiceClass {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(serializer(), this)
}