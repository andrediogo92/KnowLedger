package org.knowledger.ledger.storage.serial

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.storage.config.chainid.ChainIdBuilder

internal object ChainIdBuilderSerializationStrategy : SerializationStrategy<ChainIdBuilder>,
                                                      HashEncode {
    private val blockParamsSerialization get() = BlockParamsSerializationStrategy
    private val coinbaseParamsSerialization get() = CoinbaseParamsSerializationStrategy
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ChainId") {
            element(elementName = "ledgerHash", descriptor = hashDescriptor)
            element(elementName = "tag", descriptor = hashDescriptor)
            element(elementName = "blockParams", descriptor = blockParamsSerialization.descriptor)
            element(
                elementName = "coinbaseParams", descriptor = coinbaseParamsSerialization.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: ChainIdBuilder) {
        compositeEncode(encoder) {
            encodeHash(0, value.ledgerHash)
            encodeHash(1, value.tag)
            encodeSerializableElement(descriptor, 2, blockParamsSerialization, value.blockParams)
            encodeSerializableElement(
                descriptor, 3, coinbaseParamsSerialization, value.coinbaseParams
            )
        }
    }
}