package org.knowledger.ledger.storage.block

import kotlinx.serialization.Transient
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.service.transactions.TransactionWithBlockHash
import org.knowledger.ledger.storage.Markable
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.knowledger.ledger.storage.replace
import org.knowledger.ledger.storage.updateLinked

internal class StorageAwareBlock private constructor(
    internal val block: BlockImpl
) : Block by block, Markable by block,
    BlockUpdates, TransactionAdding,
    WitnessCalculator, StorageAware<Block> {
    internal constructor(
        adapterManager: AdapterManager,
        block: BlockImpl
    ) : this(block) {
        _invalidated = arrayOf(
            StoragePairs.Linked(
                "header",
                adapterManager.blockHeaderStorageAdapter
            ), StoragePairs.Linked(
                "coinbase",
                adapterManager.coinbaseStorageAdapter
            ), StoragePairs.Linked(
                "merkleTree",
                adapterManager.merkleTreeStorageAdapter
            ), StoragePairs.LinkedList(
                "data",
                adapterManager.transactionStorageAdapter
            )
        )
    }

    internal constructor(
        adapterManager: AdapterManager,
        ledgerInfo: LedgerInfo,
        chainId: ChainId,
        previousHash: Hash,
        blockParams: BlockParams
    ) : this(
        adapterManager,
        BlockImpl(
            mutableSortedListOf(),
            StorageAwareCoinbase(
                ledgerInfo,
                adapterManager.witnessStorageAdapter
            ),
            StorageAwareBlockHeader(
                chainId,
                ledgerInfo.hasher,
                ledgerInfo.encoder,
                previousHash,
                blockParams
            ),
            StorageAwareMerkleTree(ledgerInfo.hasher)
        )
    )


    @Transient
    override var id: StorageID? = null

    @Transient
    private var _invalidated: Array<StoragePairs<*>> = emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = _invalidated

    override fun newExtraNonce() {
        block.newExtraNonce()
        invalidateCoinbase()
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    override fun plus(transaction: Transaction): Boolean =
        addTransaction(transaction)

    override fun addTransaction(
        transaction: Transaction, checkTransaction: Boolean
    ): Boolean {
        val result = block.addTransaction(
            transaction, checkTransaction
        )
        if (result && id != null) {
            invalidated.replace(0, header)
            invalidated.replace(1, coinbase)
            invalidated.replace(2, merkleTree)
            invalidated.replace(3, transactions)
        }
        return result
    }

    override fun updateHashes() {
        block.updateHashes()
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(2, merkleTree)
        }
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        block.markForMining(blockheight, difficulty)
        invalidateCoinbase()
    }

    override fun clone(): Block =
        block.clone()

    private fun invalidateCoinbase() {
        if (id != null) {
            invalidated.replace(0, header)
            invalidated.replace(1, coinbase)
            invalidated.replace(2, merkleTree)
        }
    }

    override fun calculateWitness(
        newTransaction: Transaction,
        lastTransaction: TransactionWithBlockHash,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        previousWitnessIndex: Int,
        coinbaseHash: Hash
    ) {
        block.calculateWitness(
            newTransaction, lastTransaction, transactionOutputStorageAdapter,
            previousWitnessIndex, coinbaseHash
        )
        invalidateCoinbase()
    }

    override fun calculateWitness(
        witnessIndex: Int,
        newTransaction: Transaction,
        lastTransaction: TransactionWithBlockHash
    ) {
        block.calculateWitness(
            witnessIndex, newTransaction, lastTransaction
        )
        invalidateCoinbase()
    }

    override fun calculateWitness(
        newTransaction: Transaction,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
        previousWitnessIndex: Int,
        coinbaseHash: Hash,
        latestKnownIndex: Int,
        latestKnownHash: Hash,
        latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash
    ) {
        block.calculateWitness(
            newTransaction, transactionOutputStorageAdapter,
            previousWitnessIndex, coinbaseHash,
            latestKnownIndex, latestKnownHash,
            latestKnown, latestKnownBlockHash
        )
        invalidateCoinbase()
    }

    override fun calculateWitness(
        newTransaction: Transaction,
        witnessIndex: Int,
        latestKnownIndex: Int,
        latestKnownHash: Hash,
        latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash
    ) {
        block.calculateWitness(
            newTransaction, witnessIndex, latestKnownIndex,
            latestKnownHash, latestKnown, latestKnownBlockHash
        )
        invalidateCoinbase()
    }


    override fun equals(other: Any?): Boolean =
        block == other

    override fun hashCode(): Int =
        block.hashCode()
}