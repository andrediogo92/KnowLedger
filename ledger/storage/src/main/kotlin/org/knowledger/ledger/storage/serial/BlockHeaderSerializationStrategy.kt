package org.knowledger.ledger.storage.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.block.header.BlockHeader

internal object BlockHeaderSerializationStrategy : SerializationStrategy<BlockHeader>, HashEncode {
    private val blockParamsSerializer: SerializationStrategy<BlockParams>
        get() = BlockParamsSerializationStrategy

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("BlockHeader") {
            val seconds = PrimitiveSerialDescriptor("seconds", PrimitiveKind.LONG)
            val nonce = PrimitiveSerialDescriptor("nonce", PrimitiveKind.LONG)
            element(elementName = "chainHash", descriptor = hashDescriptor)
            element(elementName = "merkleRoot", descriptor = hashDescriptor)
            element(elementName = "previousHash", descriptor = hashDescriptor)
            element(elementName = "blockParams", descriptor = blockParamsSerializer.descriptor)
            element(elementName = seconds.serialName, descriptor = seconds)
            element(elementName = nonce.serialName, descriptor = nonce)
        }

    override fun serialize(encoder: Encoder, value: BlockHeader) {
        compositeEncode(encoder) {
            encodeHash(0, value.chainHash)
            encodeHash(1, value.merkleRoot)
            encodeHash(2, value.previousHash)
            encodeSerializableElement(descriptor, 3, blockParamsSerializer, value.blockParams)
            encodeLongElement(descriptor, 4, value.seconds)
            encodeLongElement(descriptor, 5, value.nonce)
        }
    }

}