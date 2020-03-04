package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.serial.internal.AbstractBlockHeaderSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes

internal object BlockHeaderByteSerializer : AbstractBlockHeaderSerializer(),
                                            HashEncodeInBytes {
    override val chainIdDescriptor: SerialDescriptor
        get() = ChainIdByteSerializer.descriptor

    override fun CompositeEncoder.encodeChainId(
        index: Int, chainId: ChainId
    ) {
        encodeSerializableElement(
            descriptor, index,
            ChainIdByteSerializer, chainId
        )
    }

    override fun CompositeDecoder.decodeChainId(
        index: Int
    ): ChainId =
        decodeSerializableElement(
            descriptor, index, ChainIdByteSerializer
        )
}