package org.knowledger.ledger.serial

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.list
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.ImmutableCoinbaseHeader
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.ledger.storage.witness.ImmutableWitness

interface SerializableAs<T> {
    fun <R> encode(strategy: SerializationStrategy<R>, element: R): T
    fun <R> decode(strategy: DeserializationStrategy<R>, element: T): R

    fun encodeBlock(block: ImmutableBlock): T =
        encode(ImmutableBlock.serializer(), block)

    fun encodeBlockHeader(blockHeader: ImmutableBlockHeader): T =
        encode(ImmutableBlockHeader.serializer(), blockHeader)

    fun encodeCoinbase(coinbase: ImmutableCoinbase): T =
        encode(ImmutableCoinbase.serializer(), coinbase)

    fun encodeCoinbaseHeader(coinbaseHeader: ImmutableCoinbaseHeader): T =
        encode(ImmutableCoinbaseHeader.serializer(), coinbaseHeader)

    fun encodeMerkleTree(merkleTree: ImmutableMerkleTree): T =
        encode(ImmutableMerkleTree.serializer(), merkleTree)

    fun encodeTransaction(transaction: ImmutableTransaction): T =
        encode(ImmutableTransaction.serializer(), transaction)

    fun encodeTransactions(transactions: SortedList<ImmutableTransaction>): T =
        encode(SortedListSerializer(ImmutableTransaction.serializer()), transactions)

    fun encodeTransactions(transactions: Iterable<ImmutableTransaction>): T =
        encode(ImmutableTransaction.serializer().list, transactions.toList())

    fun encodeWitness(witness: ImmutableWitness): T =
        encode(ImmutableWitness.serializer(), witness)


    fun decodeBlock(block: T): ImmutableBlock =
        decode(ImmutableBlock.serializer(), block)

    fun decodeBlockHeader(blockHeader: T): ImmutableBlockHeader =
        decode(ImmutableBlockHeader.serializer(), blockHeader)

    fun decodeCoinbase(coinbase: T): ImmutableCoinbase =
        decode(ImmutableCoinbase.serializer(), coinbase)

    fun decodeCoinbaseHeader(coinbaseHeader: T): ImmutableCoinbaseHeader =
        decode(ImmutableCoinbaseHeader.serializer(), coinbaseHeader)

    fun decodeMerkleTree(merkleTree: T): ImmutableMerkleTree =
        decode(ImmutableMerkleTree.serializer(), merkleTree)

    fun decodeTransaction(transaction: T): ImmutableTransaction =
        decode(ImmutableTransaction.serializer(), transaction)

    fun decodeTransactionsSet(transactions: T): SortedList<ImmutableTransaction> =
        decode(
            SortedListSerializer(ImmutableTransaction.serializer()),
            transactions
        )

    fun decodeTransactions(transactions: T): Iterable<ImmutableTransaction> =
        decode(ImmutableTransaction.serializer().list, transactions)

    fun decodeWitness(witness: T): ImmutableWitness =
        decode(ImmutableWitness.serializer(), witness)
}