package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass


@Serializable
data class BlockParams(
    @SerialName("blockMemorySize")
    val blockMemSize: Long = 2097152,
    val blockLength: Long = 512
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}