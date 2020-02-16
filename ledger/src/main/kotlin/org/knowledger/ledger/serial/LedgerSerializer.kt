package org.knowledger.ledger.serial

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerialModule
import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.serial.binary.BlockByteSerializer
import org.knowledger.ledger.serial.binary.BlockHeaderByteSerializer
import org.knowledger.ledger.serial.binary.CoinbaseByteSerializer
import org.knowledger.ledger.serial.binary.MerkleTreeByteSerializer
import org.knowledger.ledger.serial.binary.TransactionByteSerializer
import org.knowledger.ledger.serial.binary.TransactionOutputByteSerializer
import org.knowledger.ledger.serial.display.BlockHeaderSerializer
import org.knowledger.ledger.serial.display.BlockSerializer
import org.knowledger.ledger.serial.display.CoinbaseSerializer
import org.knowledger.ledger.serial.display.MerkleTreeSerializer
import org.knowledger.ledger.serial.display.TransactionOutputSerializer
import org.knowledger.ledger.serial.display.TransactionSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import java.util.*
import org.knowledger.ledger.results.Failure as CoreFailure

sealed class LedgerSerializer {
    data class Text(
        val serializer: StringFormat
    ) : LedgerSerializer() {

        fun encodeBlock(block: Block): String =
            serializer.stringify(BlockSerializer, block)

        fun encodeBlockHeader(blockHeader: BlockHeader): String =
            serializer.stringify(BlockHeaderSerializer, blockHeader)

        fun encodeMerkleTree(
            merkleTree: MerkleTree
        ): String =
            serializer.stringify(MerkleTreeSerializer, merkleTree)

        fun encodeTransactionOutput(
            transactionOutput: TransactionOutput
        ): String =
            serializer.stringify(
                TransactionOutputSerializer, transactionOutput
            )

        fun encodeTransaction(transaction: Transaction): String =
            serializer.stringify(TransactionSerializer, transaction)

        fun encodeTransactions(
            transactions: SortedSet<Transaction>
        ): String =
            serializer.stringify(
                SortedSetSerializer(TransactionSerializer), transactions
            )

        fun encodeTransactions(
            transactions: Iterable<Transaction>
        ): String =
            serializer.stringify(
                TransactionSerializer.list, transactions.toList()
            )


        fun encodeCoinbase(coinbase: Coinbase): String =
            serializer.stringify(CoinbaseSerializer, coinbase)


        fun encodeCompactBlock(block: Block): String =
            serializer.stringify(BlockByteSerializer, block)

        fun encodeCompactBlockHeader(
            blockHeader: BlockHeader
        ): String =
            serializer.stringify(BlockHeaderByteSerializer, blockHeader)

        fun encodeCompactMerkleTree(
            merkleTree: MerkleTree
        ): String =
            serializer.stringify(MerkleTreeByteSerializer, merkleTree)


        fun encodeCompactTransactionOutput(
            transactionOutput: TransactionOutput
        ): String =
            serializer.stringify(
                TransactionOutputByteSerializer, transactionOutput
            )

        fun encodeCompactTransaction(
            transaction: Transaction
        ): String =
            serializer.stringify(
                TransactionByteSerializer, transaction
            )

        fun encodeCompactTransactions(
            transactions: SortedSet<Transaction>
        ): String =
            serializer.stringify(
                SortedSetSerializer(TransactionByteSerializer), transactions
            )

        fun encodeCompactTransactions(
            transactions: Iterable<Transaction>
        ): String =
            serializer.stringify(
                TransactionByteSerializer.list, transactions.toList()
            )

        fun encodeCompactCoinbase(coinbase: Coinbase): String =
            serializer.stringify(CoinbaseByteSerializer, coinbase)


        fun decodeBlock(block: String): Block =
            serializer.parse(BlockSerializer, block)

        fun decodeBlockHeader(blockHeader: String): BlockHeader =
            serializer.parse(BlockHeaderSerializer, blockHeader)

        fun decodeMerkleTree(
            merkleTree: String
        ): MerkleTree =
            serializer.parse(MerkleTreeSerializer, merkleTree)

        fun decodeTransactionOutput(
            transactionOutput: String
        ): TransactionOutput =
            serializer.parse(
                TransactionOutputSerializer, transactionOutput
            )

        fun decodeTransaction(transaction: String): Transaction =
            serializer.parse(TransactionSerializer, transaction)

        fun decodeTransactionsSet(
            transactions: String
        ): SortedSet<Transaction> =
            serializer.parse(
                SortedSetSerializer(TransactionSerializer), transactions
            )

        fun decodeTransactions(
            transactions: String
        ): Iterable<Transaction> =
            serializer.parse(TransactionSerializer.list, transactions)


        fun decodeCoinbase(coinbase: String): Coinbase =
            serializer.parse(CoinbaseSerializer, coinbase)


        fun decodeCompactBlock(block: String): Block =
            serializer.parse(BlockByteSerializer, block)

        fun decodeCompactBlockHeader(
            blockHeader: String
        ): BlockHeader =
            serializer.parse(BlockHeaderByteSerializer, blockHeader)

        fun decodeCompactMerkleTree(
            merkleTree: String
        ): MerkleTree =
            serializer.parse(MerkleTreeByteSerializer, merkleTree)

        fun decodeCompactTransactionOutput(
            transactionOutput: String
        ): TransactionOutput =
            serializer.parse(
                TransactionOutputByteSerializer, transactionOutput
            )

        fun decodeCompactTransaction(
            transaction: String
        ): Transaction =
            serializer.parse(TransactionByteSerializer, transaction)

        fun decodeCompactTransactionsSet(
            transactions: String
        ): SortedSet<Transaction> =
            serializer.parse(
                SortedSetSerializer(TransactionByteSerializer), transactions
            )

        fun decodeCompactTransactions(
            transactions: String
        ): Iterable<Transaction> =
            serializer.parse(
                TransactionByteSerializer.list, transactions
            )

        fun decodeCompactCoinbase(coinbase: String): Coinbase =
            serializer.parse(CoinbaseByteSerializer, coinbase)
    }


    data class Binary(
        val serializer: BinaryFormat
    ) : LedgerSerializer() {
        fun encodeBlock(block: Block): ByteArray =
            serializer.dump(BlockSerializer, block)

        fun encodeBlockHeader(blockHeader: BlockHeader): ByteArray =
            serializer.dump(BlockHeaderSerializer, blockHeader)

        fun encodeMerkleTree(
            merkleTree: MerkleTree
        ): ByteArray =
            serializer.dump(MerkleTreeSerializer, merkleTree)

        fun encodeTransactionOutput(
            transactionOutput: TransactionOutput
        ): ByteArray =
            serializer.dump(
                TransactionOutputSerializer, transactionOutput
            )

        fun encodeTransaction(transaction: Transaction): ByteArray =
            serializer.dump(TransactionSerializer, transaction)

        fun encodeTransactions(
            transactions: SortedSet<Transaction>
        ): ByteArray =
            serializer.dump(
                SortedSetSerializer(TransactionSerializer), transactions
            )

        fun encodeTransactions(
            transactions: Iterable<Transaction>
        ): ByteArray =
            serializer.dump(
                TransactionSerializer.list, transactions.toList()
            )

        fun encodeCoinbase(coinbase: Coinbase): ByteArray =
            serializer.dump(CoinbaseSerializer, coinbase)

        fun encodeCompactBlock(block: Block): ByteArray =
            serializer.dump(BlockByteSerializer, block)

        fun encodeCompactBlockHeader(
            blockHeader: BlockHeader
        ): ByteArray =
            serializer.dump(BlockHeaderByteSerializer, blockHeader)

        fun encodeCompactMerkleTree(
            merkleTree: MerkleTree
        ): ByteArray =
            serializer.dump(MerkleTreeByteSerializer, merkleTree)

        fun encodeCompactTransactionOutput(
            transactionOutput: TransactionOutput
        ): ByteArray =
            serializer.dump(
                TransactionOutputByteSerializer, transactionOutput
            )

        fun encodeCompactTransaction(
            transaction: Transaction
        ): ByteArray =
            serializer.dump(TransactionByteSerializer, transaction)

        fun encodeCompactTransactions(
            transactions: SortedSet<Transaction>
        ): ByteArray =
            serializer.dump(
                SortedSetSerializer(TransactionByteSerializer), transactions
            )

        fun encodeCompactTransactions(
            transactions: Iterable<Transaction>
        ): ByteArray =
            serializer.dump(
                TransactionByteSerializer.list, transactions.toList()
            )

        fun encodeCompactCoinbase(coinbase: Coinbase): ByteArray =
            serializer.dump(CoinbaseByteSerializer, coinbase)


        fun decodeBlock(
            block: ByteArray
        ): Block =
            serializer.load(BlockSerializer, block)

        fun decodeBlockHeader(
            blockHeader: ByteArray
        ): BlockHeader =
            serializer.load(BlockHeaderSerializer, blockHeader)

        fun decodeMerkleTree(
            merkleTree: ByteArray
        ): MerkleTree =
            serializer.load(MerkleTreeSerializer, merkleTree)


        fun decodeTransactionOutput(
            transactionOutput: ByteArray
        ): TransactionOutput =
            serializer.load(
                TransactionOutputSerializer, transactionOutput
            )

        fun decodeTransaction(
            transaction: ByteArray
        ): Transaction =
            serializer.load(TransactionSerializer, transaction)

        fun decodeTransactionsSet(
            transactions: ByteArray
        ): SortedSet<Transaction> =
            serializer.load(SortedSetSerializer(TransactionSerializer), transactions)

        fun decodeTransactions(
            transactions: ByteArray
        ): Iterable<Transaction> =
            serializer.load(TransactionSerializer.list, transactions)

        fun decodeCoinbase(coinbase: ByteArray): Coinbase =
            serializer.load(CoinbaseSerializer, coinbase)


        fun decodeCompactBlock(
            block: ByteArray
        ): Block =
            serializer.load(BlockByteSerializer, block)

        fun decodeCompactBlockHeader(
            blockHeader: ByteArray
        ): BlockHeader =
            serializer.load(BlockHeaderByteSerializer, blockHeader)

        fun decodeCompactMerkleTree(
            merkleTree: ByteArray
        ): MerkleTree =
            serializer.load(MerkleTreeByteSerializer, merkleTree)

        fun decodeCompactTransactionOutput(
            transactionOutput: ByteArray
        ): TransactionOutput =
            serializer.load(
                TransactionOutputByteSerializer, transactionOutput
            )

        fun decodeCompactTransaction(
            transaction: ByteArray
        ): Transaction =
            serializer.load(TransactionByteSerializer, transaction)

        fun decodeCompactTransactionsSet(
            transactions: ByteArray
        ): SortedSet<Transaction> =
            serializer.load(
                SortedSetSerializer(TransactionByteSerializer), transactions
            )

        fun decodeCompactTransactions(
            transactions: ByteArray
        ): Iterable<Transaction> =
            serializer.load(
                TransactionByteSerializer.list, transactions
            )

        fun decodeCompactCoinbase(
            coinbase: ByteArray
        ): Coinbase =
            serializer.load(CoinbaseByteSerializer, coinbase)
    }

    sealed class Failure : CoreFailure {
        class NoEncoderSupplied(module: SerialModule?, encoder: SerialFormat?) : Failure() {
            override val failable: Failable =
                Failable.LightFailure(
                    "Module or Encoder not supplied: Module -> $module, Encoder -> $encoder"
                )
        }
    }
}

class LedgerTextSerializerBuilder() {
    var encoder: StringFormat? = null
    var prettyPrint: Boolean = false
    var module: SerialModule? = null

    fun build(): Outcome<LedgerSerializer.Text, LedgerSerializer.Failure> =
        when {
            module != null && encoder == null -> {
                encoder = Json(
                    configuration = JsonConfiguration(
                        prettyPrint = prettyPrint
                    ), context = module as SerialModule
                )
                Outcome.Ok(LedgerSerializer.Text(encoder!!))
            }
            module == null && encoder == null -> Outcome.Error(
                LedgerSerializer.Failure.NoEncoderSupplied(module, encoder)
            )
            encoder != null -> Outcome.Ok(
                LedgerSerializer.Text(encoder!!)
            )
            else -> deadCode()
        }
}


class LedgerBinarySerializerBuilder {
    var encoder: BinaryFormat? = null
    var module: SerialModule? = null

    fun build(): Outcome<LedgerSerializer.Binary, LedgerSerializer.Failure> =
        when {
            module != null && encoder == null -> {
                encoder = Cbor(
                    encodeDefaults = false,
                    context = module as SerialModule
                )
                Outcome.Ok(LedgerSerializer.Binary(encoder!!))
            }
            module == null && encoder == null -> Outcome.Error(
                LedgerSerializer.Failure.NoEncoderSupplied(module, encoder)
            )
            encoder != null -> Outcome.Ok(
                LedgerSerializer.Binary(encoder!!)
            )
            else -> deadCode()
        }
}


inline fun ledgerTextSerializer(
    setup: LedgerTextSerializerBuilder.() -> Unit
): Outcome<LedgerSerializer.Text, LedgerSerializer.Failure> {
    val builder = LedgerTextSerializerBuilder()
    builder.setup()
    return builder.build()
}

inline fun ledgerBinarySerializer(
    setup: LedgerBinarySerializerBuilder.() -> Unit
): Outcome<LedgerSerializer.Binary, LedgerSerializer.Failure> {
    val builder = LedgerBinarySerializerBuilder()
    builder.setup()
    return builder.build()
}