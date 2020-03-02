package org.knowledger.ledger.serial

import org.knowledger.collections.SortedList
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.Witness

interface SerializableAs<T> {
    fun encodeBlock(block: Block): T
    fun encodeBlockHeader(blockHeader: BlockHeader): T
    fun encodeCoinbase(coinbase: Coinbase): T
    fun encodeMerkleTree(merkleTree: MerkleTree): T
    fun encodeTransaction(transaction: Transaction): T
    fun encodeTransactions(transactions: SortedList<Transaction>): T
    fun encodeTransactions(transactions: Iterable<Transaction>): T
    fun encodeWitness(witness: Witness): T


    fun encodeCompactBlock(block: Block): T
    fun encodeCompactBlockHeader(blockHeader: BlockHeader): T
    fun encodeCompactCoinbase(coinbase: Coinbase): T
    fun encodeCompactMerkleTree(merkleTree: MerkleTree): T
    fun encodeCompactTransaction(transaction: Transaction): T
    fun encodeCompactTransactions(transactions: SortedList<Transaction>): T
    fun encodeCompactTransactions(transactions: Iterable<Transaction>): T
    fun encodeCompactWitness(witness: Witness): T


    fun decodeBlock(block: T): Block
    fun decodeBlockHeader(blockHeader: T): BlockHeader
    fun decodeCoinbase(coinbase: T): Coinbase
    fun decodeMerkleTree(merkleTree: T): MerkleTree
    fun decodeTransaction(transaction: T): Transaction
    fun decodeTransactionsSet(transactions: T): SortedList<Transaction>
    fun decodeTransactions(transactions: T): Iterable<Transaction>
    fun decodeWitness(witness: T): Witness


    fun decodeCompactBlock(block: T): Block
    fun decodeCompactBlockHeader(blockHeader: T): BlockHeader
    fun decodeCompactCoinbase(coinbase: T): Coinbase
    fun decodeCompactMerkleTree(merkleTree: T): MerkleTree
    fun decodeCompactTransaction(transaction: T): Transaction
    fun decodeCompactTransactionsSet(transactions: T): SortedList<Transaction>
    fun decodeCompactTransactions(transactions: T): Iterable<Transaction>
    fun decodeCompactWitness(witness: T): Witness
}