package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.BlockImpl

internal abstract class AbstractBlockSerializer(
    transactionSerializer: KSerializer<Transaction>
) : KSerializer<Block> {
    private val sortedListSerializer =
        SortedListSerializer(transactionSerializer)
    override val descriptor: SerialDescriptor =
        SerialDescriptor("Block") {
            element(
                elementName = "header",
                descriptor = blockHeaderDescriptor
            )
            element(
                elementName = "coinbase",
                descriptor = coinbaseDescriptor
            )
            element(
                elementName = "merkleTree",
                descriptor = merkleTreeDescriptor
            )
            element(
                elementName = "transactions",
                descriptor = sortedListSerializer.descriptor
            )
        }


    abstract val coinbaseDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodeCoinbase(
        index: Int, coinbase: Coinbase
    )

    abstract fun CompositeDecoder.decodeCoinbase(
        index: Int
    ): Coinbase

    abstract val blockHeaderDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodeBlockHeader(
        index: Int, header: BlockHeader
    )

    abstract fun CompositeDecoder.decodeBlockHeader(
        index: Int
    ): BlockHeader

    abstract val merkleTreeDescriptor: SerialDescriptor
    abstract fun CompositeEncoder.encodeMerkleTree(
        index: Int, merkleTree: MerkleTree
    )

    abstract fun CompositeDecoder.decodeMerkleTree(
        index: Int
    ): MerkleTree


    override fun deserialize(decoder: Decoder): Block =
        with(decoder.beginStructure(descriptor)) {
            lateinit var transactions: MutableSortedList<Transaction>
            lateinit var coinbase: Coinbase
            lateinit var header: BlockHeader
            lateinit var merkleTree: MerkleTree
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> header = decodeBlockHeader(i)
                    1 -> coinbase = decodeCoinbase(i)
                    2 -> merkleTree = decodeMerkleTree(i)
                    3 -> transactions = decodeSerializableElement(
                        descriptor, i, sortedListSerializer
                    ) as MutableSortedList<Transaction>
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            BlockImpl(
                _transactions = transactions,
                coinbase = coinbase,
                header = header,
                merkleTree = merkleTree
            )
        }

    override fun serialize(encoder: Encoder, value: Block) {
        with(encoder.beginStructure(descriptor)) {
            encodeBlockHeader(0, value.header)
            encodeCoinbase(1, value.coinbase)
            encodeMerkleTree(2, value.merkleTree)
            encodeSerializableElement(
                descriptor, 3, sortedListSerializer,
                value.transactions
            )
            endStructure(descriptor)
        }
    }
}