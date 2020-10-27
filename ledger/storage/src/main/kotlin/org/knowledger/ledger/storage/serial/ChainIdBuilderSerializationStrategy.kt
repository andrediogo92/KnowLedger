package org.knowledger.ledger.storage.serial

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.adapters.Tag
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.storage.config.chainid.ChainIdBuilder

internal object ChainIdBuilderSerializationStrategy : SerializationStrategy<ChainIdBuilder>,
                                                      HashEncode {
    private val blockParamsSerialization get() = BlockParamsSerializationStrategy
    private val coinbaseParamsSerialization get() = CoinbaseParamsSerializationStrategy
    private val tagSerializer get() = Tag.serializer()
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("ChainId") {
            element(elementName = "ledgerHash", descriptor = hashDescriptor)
            element(elementName = "tag", descriptor = tagSerializer.descriptor)
            element(elementName = "rawTag", descriptor = hashDescriptor)
            element(elementName = "blockParams", descriptor = blockParamsSerialization.descriptor)
            element(
                elementName = "coinbaseParams", descriptor = coinbaseParamsSerialization.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: ChainIdBuilder) {
        compositeEncode(encoder) {
            encodeHash(0, value.ledgerHash)
            encodeSerializableElement(descriptor, 1, tagSerializer, value.tag)
            encodeHash(2, value.rawTag)
            encodeSerializableElement(descriptor, 3, blockParamsSerialization, value.blockParams)
            encodeSerializableElement(
                descriptor, 4, coinbaseParamsSerialization, value.coinbaseParams
            )
        }
    }
}