package org.knowledger.ledger.storage.witness.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.witness.HashedWitness
import org.knowledger.ledger.storage.witness.HashedWitnessImpl
import org.knowledger.ledger.storage.witness.Witness
import org.knowledger.ledger.storage.witness.WitnessImpl

internal object HashedWitnessFactoryImpl : HashedWitnessFactory {
    private fun generateHash(
        witness: Witness, hasher: Hashers,
        encoder: BinaryFormat
    ): Hash = witness.calculateHash(hasher, encoder)

    private fun generateHash(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat
    ): Hash = generateHash(
        WitnessImpl(
            publicKey, previousWitnessIndex,
            previousCoinbase, payout,
            transactionOutputs
        ), hasher, encoder
    )


    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash,
        transactionOutput: TransactionOutput,
        hasher: Hashers, encoder: BinaryFormat
    ): HashedWitnessImpl =
        create(
            publicKey, previousWitnessIndex, previousCoinbase,
            transactionOutput.payout,
            mutableSortedListOf(transactionOutput),
            hasher, encoder
        )

    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat
    ): HashedWitnessImpl {
        val hash = generateHash(
            publicKey, previousWitnessIndex,
            previousCoinbase, payout, transactionOutputs,
            hasher, encoder
        )
        return create(
            publicKey = publicKey,
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = previousCoinbase,
            payout = payout, transactionOutputs = transactionOutputs,
            hash = hash
        )
    }

    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, index: Int
    ): HashedWitnessImpl =
        HashedWitnessImpl(
            publicKey = publicKey,
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = previousCoinbase, _payout = payout,
            _transactionOutputs = transactionOutputs, _hash = hash,
            _index = index
        )

    override fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash
    ): HashedWitnessImpl =
        HashedWitnessImpl(
            publicKey = publicKey,
            previousWitnessIndex = previousWitnessIndex,
            previousCoinbase = previousCoinbase, _payout = payout,
            _transactionOutputs = transactionOutputs, _hash = hash
        )

    override fun create(
        witness: Witness, hasher: Hashers,
        encoder: BinaryFormat
    ): HashedWitnessImpl {
        val hash = generateHash(
            witness, hasher, encoder
        )
        return create(
            publicKey = witness.publicKey,
            previousWitnessIndex = witness.previousWitnessIndex,
            previousCoinbase = witness.previousCoinbase,
            payout = witness.payout,
            transactionOutputs = witness.transactionOutputs.toMutableSortedListFromPreSorted(),
            hash = hash
        )
    }

    override fun create(witness: HashedWitness): HashedWitnessImpl =
        with(witness) {
            create(
                publicKey = publicKey,
                previousWitnessIndex = previousWitnessIndex,
                previousCoinbase = previousCoinbase,
                payout = payout,
                transactionOutputs = transactionOutputs.toMutableSortedListFromPreSorted(),
                hash = hash
            )
        }

    override fun create(other: MutableWitness): MutableWitness =
        with(other) {
            create(
                publicKey = publicKey,
                previousWitnessIndex = previousWitnessIndex,
                previousCoinbase = previousCoinbase,
                payout = payout,
                transactionOutputs = transactionOutputs.toMutableSortedListFromPreSorted(),
                hash = hash
            )
        }
}