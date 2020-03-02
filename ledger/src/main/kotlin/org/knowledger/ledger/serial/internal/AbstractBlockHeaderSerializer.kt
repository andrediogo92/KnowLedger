package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.BlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import kotlin.properties.Delegates

internal abstract class AbstractBlockHeaderSerializer : KSerializer<BlockHeader>, HashEncode {
    private object BlockHeaderSerialDescriptor : SerialClassDescImpl("BlockHeader") {
        init {
            addElement("chainId")
            addElement("merkleRoot")
            addElement("hash")
            addElement("previousHash")
            addElement("params")
            addElement("seconds")
            addElement("nonce")
        }
    }

    abstract fun CompositeEncoder.encodeChainId(index: Int, chainId: ChainId)
    abstract fun CompositeDecoder.decodeChainId(index: Int): ChainId

    override val descriptor: SerialDescriptor = BlockHeaderSerialDescriptor

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
                    0 -> chainId = decodeChainId(i)
                    1 -> merkleRoot = decodeHash(i)
                    2 -> hash = decodeHash(i)
                    3 -> previousHash = decodeHash(i)
                    4 -> blockParams = decodeSerializableElement(
                        descriptor, i, BlockParams.serializer()
                    )
                    5 -> seconds = decodeLongElement(
                        descriptor, i
                    )
                    6 -> nonce = decodeLongElement(
                        descriptor, i
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
            encodeChainId(0, obj.chainId)
            encodeHash(1, obj.merkleRoot)
            encodeHash(2, obj.hash)
            encodeHash(3, obj.previousHash)
            encodeSerializableElement(
                descriptor, 4, BlockParams.serializer(), obj.params
            )
            encodeLongElement(
                descriptor, 5, obj.seconds
            )
            encodeLongElement(
                descriptor, 6, obj.nonce
            )
            endStructure(descriptor)
        }
    }
}