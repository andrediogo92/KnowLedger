package org.knowledger.ledger.serial

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialFormat
import kotlinx.serialization.StringFormat
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import kotlinx.serialization.modules.SerialModule
import org.knowledger.collections.SortedList
import org.knowledger.ledger.results.Failable
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.serial.binary.BlockByteSerializer
import org.knowledger.ledger.serial.binary.BlockHeaderByteSerializer
import org.knowledger.ledger.serial.binary.CoinbaseByteSerializer
import org.knowledger.ledger.serial.binary.MerkleTreeByteSerializer
import org.knowledger.ledger.serial.binary.TransactionByteSerializer
import org.knowledger.ledger.serial.binary.WitnessByteSerializer
import org.knowledger.ledger.serial.display.BlockHeaderSerializer
import org.knowledger.ledger.serial.display.BlockSerializer
import org.knowledger.ledger.serial.display.CoinbaseSerializer
import org.knowledger.ledger.serial.display.MerkleTreeSerializer
import org.knowledger.ledger.serial.display.TransactionSerializer
import org.knowledger.ledger.serial.display.WitnessSerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.results.Failure as CoreFailure

sealed class LedgerSerializer {
    data class Text(
        val serializer: StringFormat
    ) : LedgerSerializer(), SerializableAs<String> {

        override fun encodeBlock(block: Block): String =
            serializer.stringify(BlockSerializer, block)

        override fun encodeBlockHeader(
            blockHeader: BlockHeader
        ): String =
            serializer.stringify(
                BlockHeaderSerializer, blockHeader
            )

        override fun encodeCoinbase(
            coinbase: Coinbase
        ): String =
            serializer.stringify(CoinbaseSerializer, coinbase)

        override fun encodeMerkleTree(
            merkleTree: MerkleTree
        ): String =
            serializer.stringify(
                MerkleTreeSerializer, merkleTree
            )

        override fun encodeTransaction(
            transaction: Transaction
        ): String =
            serializer.stringify(
                TransactionSerializer, transaction
            )

        override fun encodeTransactions(
            transactions: SortedList<Transaction>
        ): String =
            serializer.stringify(
                SortedListSerializer(TransactionSerializer),
                transactions
            )

        override fun encodeTransactions(
            transactions: Iterable<Transaction>
        ): String =
            serializer.stringify(
                TransactionSerializer.list,
                transactions.toList()
            )

        override fun encodeWitness(
            witness: Witness
        ): String =
            serializer.stringify(
                WitnessSerializer, witness
            )


        override fun encodeCompactBlock(
            block: Block
        ): String =
            serializer.stringify(
                BlockByteSerializer, block
            )

        override fun encodeCompactBlockHeader(
            blockHeader: BlockHeader
        ): String =
            serializer.stringify(
                BlockHeaderByteSerializer, blockHeader
            )

        override fun encodeCompactCoinbase(
            coinbase: Coinbase
        ): String =
            serializer.stringify(
                CoinbaseByteSerializer, coinbase
            )

        override fun encodeCompactMerkleTree(
            merkleTree: MerkleTree
        ): String =
            serializer.stringify(
                MerkleTreeByteSerializer, merkleTree
            )

        override fun encodeCompactTransaction(
            transaction: Transaction
        ): String =
            serializer.stringify(
                TransactionByteSerializer, transaction
            )

        override fun encodeCompactTransactions(
            transactions: SortedList<Transaction>
        ): String =
            serializer.stringify(
                SortedListSerializer(TransactionByteSerializer),
                transactions
            )

        override fun encodeCompactTransactions(
            transactions: Iterable<Transaction>
        ): String =
            serializer.stringify(
                TransactionByteSerializer.list,
                transactions.toList()
            )

        override fun encodeCompactWitness(
            witness: Witness
        ): String =
            serializer.stringify(
                WitnessByteSerializer, witness
            )


        override fun decodeBlock(block: String): Block =
            serializer.parse(BlockSerializer, block)

        override fun decodeBlockHeader(
            blockHeader: String
        ): BlockHeader =
            serializer.parse(
                BlockHeaderSerializer, blockHeader
            )

        override fun decodeCoinbase(
            coinbase: String
        ): Coinbase =
            serializer.parse(CoinbaseSerializer, coinbase)

        override fun decodeMerkleTree(
            merkleTree: String
        ): MerkleTree =
            serializer.parse(MerkleTreeSerializer, merkleTree)

        override fun decodeTransaction(
            transaction: String
        ): Transaction =
            serializer.parse(TransactionSerializer, transaction)

        override fun decodeTransactionsSet(
            transactions: String
        ): SortedList<Transaction> =
            serializer.parse(
                SortedListSerializer(TransactionSerializer),
                transactions
            )

        override fun decodeTransactions(
            transactions: String
        ): Iterable<Transaction> =
            serializer.parse(TransactionSerializer.list, transactions)

        override fun decodeWitness(
            witness: String
        ): Witness =
            serializer.parse(
                WitnessSerializer, witness
            )


        override fun decodeCompactBlock(
            block: String
        ): Block =
            serializer.parse(BlockByteSerializer, block)

        override fun decodeCompactBlockHeader(
            blockHeader: String
        ): BlockHeader =
            serializer.parse(
                BlockHeaderByteSerializer, blockHeader
            )

        override fun decodeCompactCoinbase(
            coinbase: String
        ): Coinbase =
            serializer.parse(CoinbaseByteSerializer, coinbase)

        override fun decodeCompactMerkleTree(
            merkleTree: String
        ): MerkleTree =
            serializer.parse(MerkleTreeByteSerializer, merkleTree)

        override fun decodeCompactTransaction(
            transaction: String
        ): Transaction =
            serializer.parse(TransactionByteSerializer, transaction)

        override fun decodeCompactTransactionsSet(
            transactions: String
        ): SortedList<Transaction> =
            serializer.parse(
                SortedListSerializer(TransactionByteSerializer),
                transactions
            )

        override fun decodeCompactTransactions(
            transactions: String
        ): Iterable<Transaction> =
            serializer.parse(
                TransactionByteSerializer.list, transactions
            )

        override fun decodeCompactWitness(
            witness: String
        ): Witness =
            serializer.parse(
                WitnessByteSerializer, witness
            )

    }


    data class Binary(
        val serializer: BinaryFormat
    ) : LedgerSerializer(), SerializableAs<ByteArray> {
        override fun encodeBlock(block: Block): ByteArray =
            serializer.dump(BlockSerializer, block)

        override fun encodeBlockHeader(
            blockHeader: BlockHeader
        ): ByteArray =
            serializer.dump(BlockHeaderSerializer, blockHeader)

        override fun encodeCoinbase(
            coinbase: Coinbase
        ): ByteArray =
            serializer.dump(CoinbaseSerializer, coinbase)

        override fun encodeMerkleTree(
            merkleTree: MerkleTree
        ): ByteArray =
            serializer.dump(MerkleTreeSerializer, merkleTree)

        override fun encodeTransaction(
            transaction: Transaction
        ): ByteArray =
            serializer.dump(TransactionSerializer, transaction)

        override fun encodeTransactions(
            transactions: SortedList<Transaction>
        ): ByteArray =
            serializer.dump(
                SortedListSerializer(TransactionSerializer),
                transactions
            )

        override fun encodeTransactions(
            transactions: Iterable<Transaction>
        ): ByteArray =
            serializer.dump(
                TransactionSerializer.list,
                transactions.toList()
            )

        override fun encodeWitness(
            witness: Witness
        ): ByteArray =
            serializer.dump(
                WitnessSerializer, witness
            )


        override fun encodeCompactBlock(
            block: Block
        ): ByteArray =
            serializer.dump(BlockByteSerializer, block)

        override fun encodeCompactBlockHeader(
            blockHeader: BlockHeader
        ): ByteArray =
            serializer.dump(
                BlockHeaderByteSerializer, blockHeader
            )

        override fun encodeCompactCoinbase(
            coinbase: Coinbase
        ): ByteArray =
            serializer.dump(CoinbaseByteSerializer, coinbase)

        override fun encodeCompactMerkleTree(
            merkleTree: MerkleTree
        ): ByteArray =
            serializer.dump(
                MerkleTreeByteSerializer, merkleTree
            )

        override fun encodeCompactTransaction(
            transaction: Transaction
        ): ByteArray =
            serializer.dump(
                TransactionByteSerializer, transaction
            )

        override fun encodeCompactTransactions(
            transactions: SortedList<Transaction>
        ): ByteArray =
            serializer.dump(
                SortedListSerializer(TransactionByteSerializer),
                transactions
            )

        override fun encodeCompactTransactions(
            transactions: Iterable<Transaction>
        ): ByteArray =
            serializer.dump(
                TransactionByteSerializer.list,
                transactions.toList()
            )

        override fun encodeCompactWitness(
            witness: Witness
        ): ByteArray =
            serializer.dump(
                WitnessByteSerializer, witness
            )


        override fun decodeBlock(block: ByteArray): Block =
            serializer.load(BlockSerializer, block)

        override fun decodeBlockHeader(
            blockHeader: ByteArray
        ): BlockHeader =
            serializer.load(BlockHeaderSerializer, blockHeader)

        override fun decodeCoinbase(
            coinbase: ByteArray
        ): Coinbase =
            serializer.load(CoinbaseSerializer, coinbase)

        override fun decodeMerkleTree(
            merkleTree: ByteArray
        ): MerkleTree =
            serializer.load(MerkleTreeSerializer, merkleTree)

        override fun decodeTransaction(
            transaction: ByteArray
        ): Transaction =
            serializer.load(TransactionSerializer, transaction)

        override fun decodeTransactionsSet(
            transactions: ByteArray
        ): SortedList<Transaction> =
            serializer.load(
                SortedListSerializer(TransactionSerializer),
                transactions
            )

        override fun decodeTransactions(
            transactions: ByteArray
        ): Iterable<Transaction> =
            serializer.load(
                TransactionSerializer.list, transactions
            )

        override fun decodeWitness(witness: ByteArray): Witness =
            serializer.load(
                WitnessSerializer, witness
            )


        override fun decodeCompactBlock(
            block: ByteArray
        ): Block =
            serializer.load(BlockByteSerializer, block)

        override fun decodeCompactBlockHeader(
            blockHeader: ByteArray
        ): BlockHeader =
            serializer.load(
                BlockHeaderByteSerializer, blockHeader
            )

        override fun decodeCompactCoinbase(
            coinbase: ByteArray
        ): Coinbase =
            serializer.load(CoinbaseByteSerializer, coinbase)

        override fun decodeCompactMerkleTree(
            merkleTree: ByteArray
        ): MerkleTree =
            serializer.load(MerkleTreeByteSerializer, merkleTree)

        override fun decodeCompactTransaction(
            transaction: ByteArray
        ): Transaction =
            serializer.load(TransactionByteSerializer, transaction)

        override fun decodeCompactTransactionsSet(
            transactions: ByteArray
        ): SortedList<Transaction> =
            serializer.load(
                SortedListSerializer(TransactionByteSerializer),
                transactions
            )

        override fun decodeCompactTransactions(
            transactions: ByteArray
        ): Iterable<Transaction> =
            serializer.load(
                TransactionByteSerializer.list, transactions
            )

        override fun decodeCompactWitness(
            witness: ByteArray
        ): Witness =
            serializer.load(
                WitnessByteSerializer, witness
            )
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

class LedgerTextSerializerBuilder {
    var encoder: StringFormat? = null
    var prettyPrint: Boolean = false
    var module: SerialModule? = null

    @UseExperimental(UnstableDefault::class)
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