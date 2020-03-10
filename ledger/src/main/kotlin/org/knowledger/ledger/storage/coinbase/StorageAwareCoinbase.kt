package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.toEncoded
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.*
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.adapters.WitnessStorageAdapter
import org.knowledger.ledger.storage.transaction.output.StorageAwareTransactionOutput
import org.knowledger.ledger.storage.witness.StorageAwareWitness

internal class StorageAwareCoinbase private constructor(
    internal val coinbase: HashedCoinbaseImpl
) : HashedCoinbase by coinbase,
    Markable by coinbase,
    NonceRegen, WitnessAdding,
    StorageAware<HashedCoinbase> {
    private var _invalidated: Array<StoragePairs<*>> =
        emptyArray()

    override val invalidated: Array<StoragePairs<*>>
        get() = _invalidated

    override var id: StorageID? = null

    internal constructor(
        coinbase: HashedCoinbaseImpl,
        witnessStorageAdapter: WitnessStorageAdapter
    ) : this(coinbase) {
        _invalidated = arrayOf(
            StoragePairs.Native("extraNonce"),
            StoragePairs.Hash("hash"),
            StoragePairs.LinkedList(
                "witnesses",
                witnessStorageAdapter
            ), StoragePairs.Native("blockheight"),
            StoragePairs.Difficulty("difficulty")
        )
    }

    internal constructor(
        info: LedgerInfo,
        witnessStorageAdapter: WitnessStorageAdapter
    ) : this(HashedCoinbaseImpl(info), witnessStorageAdapter)

    override fun newNonce() {
        coinbase.newNonce()
        if (id != null) {
            invalidated.replace(0, extraNonce)
            invalidated.replace(1, hash)
        }
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        coinbase.markForMining(blockheight, difficulty)
        if (id != null) {
            invalidated.replace(1, hash)
            invalidated.replace(3, blockheight)
            invalidated.replace(4, difficulty)
        }
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)

    fun calculateTransactionOutput(
        payout: Payout,
        newIndex: Int,
        newTransaction: Hash,
        previousBlock: Hash,
        previousIndex: Int,
        previousTransaction: Hash
    ): StorageAwareTransactionOutput =
        StorageAwareTransactionOutput(
            coinbase.calculateTransactionOutput(
                payout = payout, newIndex = newIndex,
                newTransaction = newTransaction,
                previousBlock = previousBlock,
                previousIndex = previousIndex,
                previousTransaction = previousTransaction
            )
        )

    fun calculateWitness(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int,
        previousCoinbase: Hash, transactionOutput: TransactionOutput,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter
    ): StorageAwareWitness =
        StorageAwareWitness(
            transactionOutputStorageAdapter = transactionOutputStorageAdapter,
            witness = coinbase.calculateWitness(
                publicKey = publicKey, previousWitnessIndex = previousWitnessIndex,
                previousCoinbase = previousCoinbase,
                transactionOutput = transactionOutput
            )
        )

    fun calculateWitness(
        publicKey: EncodedPublicKey, payout: Payout,
        previousWitnessIndex: Int, previousCoinbase: Hash,
        newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int,
        previousTransaction: Hash,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter
    ): StorageAwareWitness =
        calculateWitness(
            publicKey, previousWitnessIndex, previousCoinbase,
            calculateTransactionOutput(
                payout = payout,
                newIndex = newIndex, newTransaction = newTransaction,
                previousBlock = previousBlock,
                previousIndex = previousIndex,
                previousTransaction = previousTransaction
            ), transactionOutputStorageAdapter = transactionOutputStorageAdapter
        )

    override fun addToWitness(
        witness: Witness,
        newIndex: Int,
        newTransaction: Transaction,
        latestKnownIndex: Int,
        latestKnownHash: Hash,
        latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash
    ) {
        coinbase.calculateThenAdd(newTransaction, latestKnown) { payoutToAdd ->
            coinbase.addToOutputs(witness = witness) {
                calculateTransactionOutput(
                    payout = payoutToAdd,
                    newIndex = newIndex,
                    newTransaction = newTransaction.hash,
                    previousBlock = latestKnownBlockHash,
                    previousIndex = latestKnownIndex,
                    previousTransaction = latestKnownHash
                )
            }
        }
    }

    override fun addToWitness(
        newIndex: Int,
        newTransaction: Transaction,
        previousWitnessIndex: Int,
        latestCoinbase: Hash,
        latestKnownIndex: Int,
        latestKnownHash: Hash,
        latestKnown: PhysicalData?,
        latestKnownBlockHash: Hash,
        transactionOutputStorageAdapter: TransactionOutputStorageAdapter?
    ) {
        coinbase.calculateThenAdd(newTransaction, latestKnown) { payoutToAdd ->
            coinbase.addToOutputs {
                calculateWitness(
                    payout = payoutToAdd,
                    newIndex = newIndex,
                    newTransaction = newTransaction.hash,
                    previousBlock = latestKnownBlockHash,
                    previousIndex = latestKnownIndex,
                    previousTransaction = latestKnownHash,
                    publicKey = newTransaction.publicKey.toEncoded(),
                    previousWitnessIndex = previousWitnessIndex,
                    previousCoinbase = latestCoinbase,
                    transactionOutputStorageAdapter = transactionOutputStorageAdapter!!
                )
            }
        }
        if (id != null) {
            invalidated.replace(2, witnesses)
        }
    }

    override fun equals(other: Any?): Boolean =
        coinbase == other

    override fun hashCode(): Int =
        coinbase.hashCode()
}