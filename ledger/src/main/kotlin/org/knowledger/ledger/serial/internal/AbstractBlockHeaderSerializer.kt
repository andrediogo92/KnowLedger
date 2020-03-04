package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.BlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import kotlin.properties.Delegates

internal abstract class AbstractBlockHeaderSerializer : KSerializer<BlockHeader>, HashEncode {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("BlockHeader") {
            val seconds = PrimitiveDescriptor(
                "seconds", PrimitiveKind.LONG
            )
            val nonce = PrimitiveDescriptor(
                "nonce", PrimitiveKind.LONG
            )
            element(
                elementName = "chainId",
                descriptor = chainIdDescriptor
            )
            element(
                elementName = "merkleRoot",
                descriptor = hashDescriptor
            )
            element(
                elementName = "hash",
                descriptor = hashDescriptor
            )
            element(
                elementName = "previousHash",
                descriptor = hashDescriptor
            )
            element(
                elementName = "params",
                descriptor = BlockParams.serializer().descriptor
            )
            element(
                elementName = seconds.serialName,
                descriptor = seconds
            )
            element(
                elementName = nonce.serialName,
                descriptor = nonce
            )
        }

    abstract val chainIdDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodeChainId(
        index: Int, chainId: ChainId
    )

    abstract fun CompositeDecoder.decodeChainId(
        index: Int
    ): ChainId

    override fun deserialize(decoder: Decoder): BlockHeader =
        with(decoder.beginStructure(descriptor)) {
            lateinit var chainId: ChainId
            lateinit var merkleRoot: Hash
            lateinit var hash: Hash
            lateinit var previousHash: Hash
            lateinit var blockParams: BlockParams
            var seconds by Delegates.notNull<Long>()
            var nonce by Delegates.notNull<Long>()
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

    override fun serialize(encoder: Encoder, value: BlockHeader) {
        with(encoder.beginStructure(descriptor)) {
            encodeChainId(0, value.chainId)
            encodeHash(1, value.merkleRoot)
            encodeHash(2, value.hash)
            encodeHash(3, value.previousHash)
            encodeSerializableElement(
                descriptor, 4, BlockParams.serializer(), value.params
            )
            encodeLongElement(
                descriptor, 5, value.seconds
            )
            encodeLongElement(
                descriptor, 6, value.nonce
            )
            endStructure(descriptor)
        }
    }
}