package org.knowledger.ledger.storage.config.block

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
@SerialName("BlockParams")
data class ImmutableBlockParams(
    override val blockMemorySize: Int,
    override val blockLength: Int,
) : BlockParams {
    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(serializer(), this)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BlockParams) return false


        if (blockMemorySize != other.blockMemorySize) return false
        if (blockLength != other.blockLength) return false

        return true
    }

    override fun hashCode(): Int {
        var result = blockMemorySize
        result = 31 * result + blockLength
        return result
    }


}