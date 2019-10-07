package org.knowledger.ledger.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.hashFromHexString
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.BlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import kotlin.properties.Delegates

@Serializer(forClass = BlockHeader::class)
object BlockHeaderSerializer : KSerializer<BlockHeader> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("BlockHeader") {
            init {
                addElement("chainId")
                addElement("merkleRoot")
                addElement("previousHash")
                addElement("params")
                addElement("seconds")
                addElement("nonce")
                addElement("hash")
            }
        }

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
                        descriptor, i, ChainIdSerializer
                    )
                    1 -> merkleRoot = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
                    2 -> previousHash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
                    3 -> blockParams = decodeSerializableElement(
                        descriptor, i, BlockParams.serializer()
                    )
                    4 -> seconds = decodeLongElement(
                        descriptor, i
                    )
                    5 -> nonce = decodeLongElement(
                        descriptor, i
                    )
                    6 -> hash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
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
                descriptor, 0, ChainIdSerializer, obj.chainId
            )
            encodeStringElement(
                descriptor, 1, obj.merkleRoot.print
            )
            encodeStringElement(
                descriptor, 2, obj.previousHash.print
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
            encodeStringElement(
                descriptor, 6, obj.hash.print
            )
            endStructure(descriptor)
        }
    }
}