package org.knowledger.ledger.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.core.base.hash.hashFromHexString
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.Tag

@Serializer(forClass = ChainId::class)
object ChainIdSerializer : KSerializer<ChainId> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("ChainId") {
            init {
                addElement("tag")
                addElement("ledgerHash")
                addElement("hash")
            }
        }

    override fun deserialize(decoder: Decoder): ChainId =
        with(decoder.beginStructure(descriptor)) {
            lateinit var tag: Tag
            lateinit var ledgerHash: Hash
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> tag = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
                    1 -> ledgerHash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
                    2 -> hash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            ChainIdImpl(tag, ledgerHash, hash)
        }

    override fun serialize(encoder: Encoder, obj: ChainId) {
        with(encoder.beginStructure(descriptor)) {
            encodeStringElement(
                descriptor, 0, obj.tag.toHexString()
            )
            encodeStringElement(
                descriptor, 1, obj.ledgerHash.toHexString()
            )
            encodeStringElement(
                descriptor, 2, obj.hash.toHexString()
            )
            endStructure(descriptor)
        }
    }
}