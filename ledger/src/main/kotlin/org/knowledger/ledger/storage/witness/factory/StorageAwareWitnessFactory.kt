package org.knowledger.ledger.storage.witness.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import org.knowledger.ledger.storage.witness.HashedWitness
import org.knowledger.ledger.storage.witness.MutableHashedWitness
import org.knowledger.ledger.storage.witness.StorageAwareWitnessImpl
import org.knowledger.ledger.storage.witness.Witness

internal class StorageAwareWitnessFactory(
    internal val transactionOutputStorageAdapter: TransactionOutputStorageAdapter,
    private val factory: HashedWitnessFactory = HashedWitnessFactoryImpl
) : HashedWitnessFactory {
    private fun createSA(
        witness: MutableHashedWitness
    ): StorageAwareWitnessImpl = StorageAwareWitnessImpl(
        witness = witness, invalidated = arrayOf(
            StoragePairs.Hash("hash"),
            StoragePairs.Native("index"),
            StoragePairs.LinkedList(
                "transactionOutputs",
                transactionOutputStorageAdapter
            )
        )
    )

    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash,
        transactionOutput: TransactionOutput,
        hasher: Hashers, encoder: BinaryFormat
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex,
            previousCoinbase, transactionOutput,
            hasher, encoder
        )
    )


    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex,
            previousCoinbase, payout,
            transactionOutputs, hasher, encoder
        )
    )

    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex,
            previousCoinbase, payout,
            transactionOutputs, hash
        )
    )

    override fun create(
        witness: Witness, hasher: Hashers,
        encoder: BinaryFormat
    ): MutableHashedWitness = createSA(
        factory.create(
            witness, hasher, encoder
        )
    )

    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, index: Int
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex,
            previousCoinbase, payout,
            transactionOutputs,
            hash, index
        )
    )

    override fun create(
        witness: HashedWitness
    ): StorageAwareWitnessImpl = createSA(
        factory.create(witness)
    )

    override fun create(
        other: MutableHashedWitness
    ): StorageAwareWitnessImpl = createSA(
        factory.create(other)
    )
}