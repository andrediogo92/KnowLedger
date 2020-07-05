package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass


@Serializable
data class BlockParams(
    val blockMemorySize: Int = 2097152,
    val blockLength: Int = 512
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}