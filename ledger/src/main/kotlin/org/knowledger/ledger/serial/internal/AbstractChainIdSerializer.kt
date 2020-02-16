package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.Tag

internal abstract class AbstractChainIdSerializer : KSerializer<ChainId>, HashEncode {
    object ChainIdSerialDescriptor : SerialClassDescImpl("ChainId") {
        init {
            addElement("tag")
            addElement("ledgerHash")
            addElement("hash")
        }
    }

    override val descriptor: SerialDescriptor = ChainIdSerialDescriptor

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

    override fun serialize(encoder: Encoder, obj: ChainId) {
        with(encoder.beginStructure(descriptor)) {
            encodeHash(0, obj.tag)
            encodeHash(1, obj.ledgerHash)
            encodeHash(2, obj.hash)
            endStructure(descriptor)
        }
    }
}