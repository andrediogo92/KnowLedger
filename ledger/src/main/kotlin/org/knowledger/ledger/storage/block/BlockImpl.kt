package org.knowledger.ledger.storage.block

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Transient
import kotlinx.serialization.cbor.Cbor
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.collections.copyMutableSortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.hash.Hashers.Companion.DEFAULT_HASHER
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.binary.BlockByteSerializer
import org.knowledger.ledger.service.transactions.TransactionWithBlockHash
import org.knowledger.ledger.storage.*
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.blockheader.MerkleTreeUpdate
import org.knowledger.ledger.storage.coinbase.WitnessAdding
import org.tinylog.kotlin.Logger

internal data class BlockImpl(
    private val _transactions: MutableSortedList<Transaction>,
    override val coinbase: Coinbase,
    override val header: BlockHeader,
    override var merkleTree: MerkleTree,
    @Transient
    internal var encoder: BinaryFormat = Cbor,
    @Transient
    internal var hasher: Hashers = DEFAULT_HASHER
) : Block, BlockUpdates, Markable, WitnessCalculator,
    TransactionAdding, LedgerContract {
    @Transient
    internal var cachedSize: Long? = null

    override val transactions: SortedList<Transaction>
        get() = _transactions

    override val approximateSize: Long
        get() = cachedSize ?: recalculateApproximateSize()

    override fun newExtraNonce() {
        (coinbase as NonceRegen).newNonce()
        merkleTree.buildFromCoinbase(coinbase)
        (header as MerkleTreeUpdate).updateMerkleTree(merkleTree.hash)
    }

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(BlockByteSerializer, this)

    override fun clone(): Block =
        copy(
            _transactions = _transactions.copyMutableSortedList {
                it.clone()
            },
            header = header.clone(),
            coinbase = coinbase.clone(),
            merkleTree = merkleTree.clone()
        )

    /**
     * Add a single new transaction.
     *
     * Checks if block is sized correctly.
     *
     * Checks if the transaction is valid.
     *
     * @param transaction   Transaction to attempt to add to the block.
     * @return Whether the transaction was valid and cprrectly inserted.
     */
    override fun plus(transaction: Transaction): Boolean =
        addTransaction(transaction)

    override fun addTransaction(
        transaction: Transaction, checkTransaction: Boolean
    ): Boolean {
        val transactionSize =
            transaction.approximateSize(encoder)
        val cumSize = approximateSize + transactionSize
        if (cumSize < header.params.blockMemorySize) {
            if (_transactions.size < header.params.blockLength) {
                //If we don't have to check the transaction,
                //Skip processing signatures.
                if (!checkTransaction || transaction.processTransaction(encoder)) {
                    _transactions.add(transaction)
                    _transactions.forEachIndexed { i, elem ->
                        (elem as Indexed).markIndex(i)
                    }
                    cachedSize = cumSize
                    Logger.debug {
                        "Transaction Successfully added to Block"
                    }
                    return true
                }
            }
        }
        Logger.debug {
            "Transaction failed to process. Discarded."
        }
        return false
    }

    override fun calculateWitness(
        newTransaction: Transaction,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        previousWitnessIndex: Int,
        coinbaseHash: Hash, latestKnownIndex: Int,
        latestKnownHash: Hash, latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash
    ) {
        val index = transactions.binarySearch(newTransaction)
        (coinbase as WitnessAdding).addToWitness(
            newIndex = index, newTransaction = newTransaction,
            previousWitnessIndex = previousWitnessIndex,
            latestCoinbase = coinbaseHash,
            latestKnownIndex = latestKnownIndex,
            latestKnown = latestKnown,
            latestKnownHash = latestKnownHash,
            latestKnownBlockHash = latestKnownBlockHash,
            transactionOutputStorageAdapter = transactionOutputStorageAdapter
        )
    }

    override fun calculateWitness(
        newTransaction: Transaction,
        lastTransaction: TransactionWithBlockHash,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        previousWitnessIndex: Int, coinbaseHash: Hash
    ) {
        val index = transactions.binarySearch(newTransaction)
        (coinbase as WitnessAdding).addToWitness(
            newIndex = index, newTransaction = newTransaction,
            previousWitnessIndex = previousWitnessIndex, latestCoinbase = coinbaseHash,
            latestKnownIndex = lastTransaction.txIndex,
            latestKnown = lastTransaction.txData,
            latestKnownHash = lastTransaction.txHash,
            latestKnownBlockHash = lastTransaction.txBlockHash,
            transactionOutputStorageAdapter = transactionOutputStorageAdapter
        )
    }

    override fun calculateWitness(
        witnessIndex: Int, newTransaction: Transaction,
        lastTransaction: TransactionWithBlockHash
    ) {
        val index = transactions.binarySearch(newTransaction)
        (coinbase as WitnessAdding).addToWitness(
            witness = coinbase.witnesses[witnessIndex],
            newIndex = index, newTransaction = newTransaction,
            latestKnownIndex = lastTransaction.txIndex,
            latestKnown = lastTransaction.txData,
            latestKnownHash = lastTransaction.txHash,
            latestKnownBlockHash = lastTransaction.txBlockHash
        )
    }

    override fun calculateWitness(
        newTransaction: Transaction, witnessIndex: Int,
        latestKnownIndex: Int, latestKnownHash: Hash,
        latestKnown: PhysicalData?, latestKnownBlockHash: Hash
    ) {
        val index = transactions.binarySearch(newTransaction)
        (coinbase as WitnessAdding).addToWitness(
            witness = coinbase.witnesses[witnessIndex],
            newIndex = index, newTransaction = newTransaction,
            latestKnownIndex = latestKnownIndex,
            latestKnown = latestKnown,
            latestKnownHash = latestKnownHash,
            latestKnownBlockHash = latestKnownBlockHash
        )
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        (coinbase as Markable).markForMining(blockheight, difficulty)
    }


    override fun updateHashes() {
        merkleTree.rebuildMerkleTree(coinbase, _transactions.toTypedArray())
        (header as MerkleTreeUpdate).updateMerkleTree(merkleTree.hash)
    }


    override fun verifyTransactions(): Boolean {
        return merkleTree.verifyBlockTransactions(
            coinbase,
            _transactions.toTypedArray()
        )
    }

    /**
     * Recalculates the entire block size.
     *
     * Is very time consuming and only necessary if:
     *
     * 1. There is a need to calculate the effective block size after deserialization;
     * 2. There is a need to calculate the effective block size after retrieval
     *         of a block from a database.
     */
    fun recalculateApproximateSize(): Long {
        cachedSize = serialize(encoder).size.toLong()
        return cachedSize as Long
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Block) return false

        if (_transactions != other.transactions) return false
        if (coinbase != other.coinbase) return false
        if (header != other.header) return false
        if (merkleTree != other.merkleTree) return false

        return true
    }

    override fun hashCode(): Int {
        var result = _transactions.hashCode()
        result = 31 * result + coinbase.hashCode()
        result = 31 * result + header.hashCode()
        result = 31 * result + merkleTree.hashCode()
        return result
    }


}