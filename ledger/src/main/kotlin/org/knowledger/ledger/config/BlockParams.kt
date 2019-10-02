package org.knowledger.ledger.config

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.service.ServiceClass


@Serializable
@SerialName("BlockParams")
data class BlockParams(
    @SerialName("blockMemorySize")
    val blockMemSize: Long = 2097152,
    @SerialName("blockLength")
    val blockLength: Long = 512
) : HashSerializable, ServiceClass {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)
}