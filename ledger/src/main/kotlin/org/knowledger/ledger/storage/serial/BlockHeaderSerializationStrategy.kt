package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.serial.ChainIdSerializer
import org.knowledger.ledger.serial.HashEncode
import org.knowledger.ledger.serial.compositeEncode
import org.knowledger.ledger.storage.block.header.BlockHeader

internal object BlockHeaderSerializationStrategy : SerializationStrategy<BlockHeader>,
                                                   HashEncode {
    val blockParamsSerializer: SerializationStrategy<BlockParams> =
        BlockParams.serializer()
    val chainIdSerializer: SerializationStrategy<ChainId>
        get() = ChainIdSerializer

    override val descriptor: SerialDescriptor =
        SerialDescriptor("BlockHeader") {
            val seconds = PrimitiveDescriptor(
                "seconds", PrimitiveKind.LONG
            )
            val nonce = PrimitiveDescriptor(
                "nonce", PrimitiveKind.LONG
            )
            element(
                elementName = "chainId",
                descriptor = chainIdSerializer.descriptor
            )
            element(
                elementName = "merkleRoot",
                descriptor = hashDescriptor
            )
            element(
                elementName = "previousHash",
                descriptor = hashDescriptor
            )
            element(
                elementName = "params",
                descriptor = blockParamsSerializer.descriptor
            )
            element(
                elementName = seconds.serialName,
                descriptor = seconds
            )
            element(
                elementName = nonce.serialName,
                descriptor = nonce
            )
        }

    override fun serialize(encoder: Encoder, value: BlockHeader) {
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0, chainIdSerializer, value.chainId
            )
            encodeHash(1, value.merkleRoot)
            encodeHash(2, value.previousHash)
            encodeSerializableElement(
                descriptor, 3, BlockParams.serializer(), value.params
            )
            encodeLongElement(
                descriptor, 4, value.seconds
            )
            encodeLongElement(
                descriptor, 5, value.nonce
            )
        }
    }

}