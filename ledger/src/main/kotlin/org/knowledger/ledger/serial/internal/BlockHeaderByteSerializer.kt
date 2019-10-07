package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.serial.BlockHeaderSerializer
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.BlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import kotlin.properties.Delegates

@Serializer(forClass = BlockHeader::class)
object BlockHeaderByteSerializer : KSerializer<BlockHeader> {
    override val descriptor: SerialDescriptor =
        BlockHeaderSerializer.descriptor

    override fun deserialize(decoder: Decoder): BlockHeader =
        with(decoder.beginStructure(descriptor)) {
            lateinit var chainId: ChainId
            lateinit var merkleRoot: Hash
            // Difficulty is fixed at block generation time.
            lateinit var previousHash: Hash
            lateinit var blockParams: BlockParams
            var seconds by Delegates.notNull<Long>()
            var nonce by Delegates.notNull<Long>()
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> chainId = decodeSerializableElement(
                        descriptor, i, ChainIdByteSerializer
                    )
                    1 -> merkleRoot = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    2 -> previousHash = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    3 -> blockParams = decodeSerializableElement(
                        descriptor, i, BlockParams.serializer()
                    )
                    4 -> seconds = decodeLongElement(
                        descriptor, i
                    )
                    5 -> nonce = decodeLongElement(
                        descriptor, i
                    )
                    6 -> hash = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedBlockHeaderImpl(
                BlockHeaderImpl(
                    chainId = chainId,
                    _merkleRoot = merkleRoot,
                    previousHash = previousHash,
                    params = blockParams,
                    seconds = seconds,
                    _nonce = nonce
                ), hash
            )
        }

    override fun serialize(encoder: Encoder, obj: BlockHeader) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, ChainIdByteSerializer, obj.chainId
            )
            encodeSerializableElement(
                descriptor, 1, HashSerializer, obj.merkleRoot
            )
            encodeSerializableElement(
                descriptor, 2, HashSerializer, obj.previousHash
            )
            encodeSerializableElement(
                descriptor, 3, BlockParams.serializer(), obj.params
            )
            encodeLongElement(
                descriptor, 4, obj.seconds
            )
            encodeLongElement(
                descriptor, 5, obj.nonce
            )
            encodeSerializableElement(
                descriptor, 6, HashSerializer, obj.hash
            )
            endStructure(descriptor)
        }
    }
}