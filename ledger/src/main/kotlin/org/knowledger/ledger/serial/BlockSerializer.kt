package org.knowledger.ledger.serial

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.block.BlockImpl
import java.util.*

@Serializer(forClass = Block::class)
object BlockSerializer : KSerializer<Block> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("Block") {
            init {
                addElement("transactions")
                addElement("coinbase")
                addElement("header")
                addElement("merkleTree")
            }
        }

    private val sortedSetSerializer = SortedSetSerializer(TransactionSerializer)

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
                        descriptor, i, CoinbaseSerializer
                    )
                    2 -> header = decodeSerializableElement(
                        descriptor, i, BlockHeaderSerializer
                    )
                    3 -> merkleTree = decodeSerializableElement(
                        descriptor, i, MerkleTreeSerializer
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
                descriptor, 1, CoinbaseSerializer, obj.coinbase
            )
            encodeSerializableElement(
                descriptor, 2, BlockHeaderSerializer, obj.header
            )
            encodeSerializableElement(
                descriptor, 3, MerkleTreeSerializer, obj.merkleTree
            )
            endStructure(descriptor)
        }
    }
}