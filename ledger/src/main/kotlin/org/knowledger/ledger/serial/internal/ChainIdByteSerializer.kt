package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ChainIdImpl
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.serial.ChainIdSerializer

@Serializer(forClass = ChainId::class)
internal object ChainIdByteSerializer : KSerializer<ChainId> {
    override val descriptor: SerialDescriptor =
        ChainIdSerializer.descriptor

    override fun deserialize(decoder: Decoder): ChainId =
        with(decoder.beginStructure(descriptor)) {
            lateinit var tag: Tag
            lateinit var ledgerHash: Hash
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> tag = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    1 -> ledgerHash = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    2 -> hash = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            ChainIdImpl(tag, ledgerHash, hash)
        }

    override fun serialize(encoder: Encoder, obj: ChainId) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, HashSerializer, obj.tag
            )
            encodeSerializableElement(
                descriptor, 1, HashSerializer, obj.ledgerHash
            )
            encodeSerializableElement(
                descriptor, 2, HashSerializer, obj.hash
            )
            endStructure(descriptor)
        }
    }
}