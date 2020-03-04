package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Tag

internal abstract class AbstractChainIdSerializer : KSerializer<ChainId>, HashEncode {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("ChainId") {
            element(
                elementName = "tag",
                descriptor = hashDescriptor
            )
            element(
                elementName = "ledgerHash",
                descriptor = hashDescriptor
            )
            element(
                elementName = "hash",
                descriptor = hashDescriptor
            )
        }

    override fun deserialize(decoder: Decoder): ChainId =
        with(decoder.beginStructure(descriptor)) {
            lateinit var tag: Tag
            lateinit var ledgerHash: Hash
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> tag = decodeHash(i)
                    1 -> ledgerHash = decodeHash(i)
                    2 -> hash = decodeHash(i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            ChainIdImpl(tag, ledgerHash, hash)
        }

    override fun serialize(encoder: Encoder, value: ChainId) {
        with(encoder.beginStructure(descriptor)) {
            encodeHash(0, value.tag)
            encodeHash(1, value.ledgerHash)
            encodeHash(2, value.hash)
            endStructure(descriptor)
        }
    }
}