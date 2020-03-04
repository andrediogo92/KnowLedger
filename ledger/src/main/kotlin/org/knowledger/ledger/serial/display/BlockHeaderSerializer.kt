package org.knowledger.ledger.serial.display

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.serial.internal.AbstractBlockHeaderSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay

internal object BlockHeaderSerializer : AbstractBlockHeaderSerializer(),
                                        HashEncodeForDisplay {
    override val chainIdDescriptor: SerialDescriptor
        get() = ChainIdSerializer.descriptor

    override fun CompositeEncoder.encodeChainId(
        index: Int, chainId: ChainId
    ) {
        encodeSerializableElement(
            descriptor, index,
            ChainIdSerializer, chainId
        )
    }

    override fun CompositeDecoder.decodeChainId(
        index: Int
    ): ChainId =
        decodeSerializableElement(
            descriptor, index, ChainIdSerializer
        )
}