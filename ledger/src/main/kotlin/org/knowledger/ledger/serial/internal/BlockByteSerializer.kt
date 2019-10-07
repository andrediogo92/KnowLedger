package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import org.knowledger.ledger.serial.BlockSerializer
import org.knowledger.ledger.serial.SortedSetSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.BlockImpl
import java.util.*

@Serializer(forClass = Block::class)
object BlockByteSerializer : KSerializer<Block> {
    override val descriptor: SerialDescriptor =
        BlockSerializer.descriptor

    private val sortedSetSerializer =
        SortedSetSerializer(TransactionByteSerializer)

    override fun deserialize(decoder: Decoder): Block =
        with(decoder.beginStructure(descriptor)) {
            lateinit var transactions: SortedSet<Transaction>
            lateinit var coinbase: Coinbase
            lateinit var header: BlockHeader
            lateinit var merkleTree: MerkleTree
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> transactions = decodeSerializableElement(
                        descriptor, i, sortedSetSerializer
                    )
                    1 -> coinbase = decodeSerializableElement(
                        descriptor, i, CoinbaseByteSerializer
                    )
                    2 -> header = decodeSerializableElement(
                        descriptor, i, BlockHeaderByteSerializer
                    )
                    3 -> merkleTree = decodeSerializableElement(
                        descriptor, i, MerkleTreeByteSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            BlockImpl(
                transactions = transactions,
                coinbase = coinbase,
                header = header,
                merkleTree = merkleTree
            )
        }

    override fun serialize(encoder: Encoder, obj: Block) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, sortedSetSerializer,
                obj.transactions
            )
            encodeSerializableElement(
                descriptor, 1, CoinbaseByteSerializer, obj.coinbase
            )
            encodeSerializableElement(
                descriptor, 2, BlockHeaderByteSerializer, obj.header
            )
            encodeSerializableElement(
                descriptor, 3, MerkleTreeByteSerializer, obj.merkleTree
            )
            endStructure(descriptor)
        }
    }
}