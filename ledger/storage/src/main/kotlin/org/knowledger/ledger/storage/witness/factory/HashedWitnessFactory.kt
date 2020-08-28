package org.knowledger.ledger.storage.witness.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.witness.HashedWitnessImpl
import org.knowledger.ledger.storage.witness.WitnessImpl
import org.knowledger.ledger.storage.witness.Witness as UnhashedWitness

@OptIn(ExperimentalSerializationApi::class)
internal class HashedWitnessFactory : WitnessFactory {
    private fun generateHash(
        witness: UnhashedWitness, hasher: Hashers, encoder: BinaryFormat,
    ): Hash = witness.calculateHash(hasher, encoder)

    private fun generateHash(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat,
    ): Hash = generateHash(
        WitnessImpl(publicKey, previousWitnessIndex, previousCoinbase, payout, transactionOutputs),
        hasher, encoder
    )


    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        transactionOutput: TransactionOutput, hasher: Hashers, encoder: BinaryFormat,
    ): HashedWitnessImpl = create(
        publicKey, previousWitnessIndex, previousCoinbase, transactionOutput.payout,
        mutableSortedListOf(transactionOutput), hasher, encoder
    )

    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat,
    ): HashedWitnessImpl {
        val hash = generateHash(
            publicKey, previousWitnessIndex, previousCoinbase, payout,
            transactionOutputs, hasher, encoder
        )
        return create(
            publicKey, previousWitnessIndex, previousCoinbase, payout, transactionOutputs, hash
        )
    }

    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, index: Int,
    ): HashedWitnessImpl = HashedWitnessImpl(
        publicKey, previousWitnessIndex, previousCoinbase,
        payout, transactionOutputs, hash, index
    )

    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>, hash: Hash,
    ): HashedWitnessImpl = HashedWitnessImpl(
        publicKey, previousWitnessIndex, previousCoinbase, payout, transactionOutputs, hash
    )

    override fun create(
        witness: UnhashedWitness, hasher: Hashers, encoder: BinaryFormat,
    ): HashedWitnessImpl =
        with(witness) {
            val hash = generateHash(this, hasher, encoder)
            create(
                publicKey, previousWitnessIndex, previousCoinbase, payout,
                transactionOutputs.toMutableSortedListFromPreSorted(), hash
            )
        }

    override fun create(witness: Witness): HashedWitnessImpl =
        with(witness) {
            create(
                publicKey, previousWitnessIndex, previousCoinbase, payout,
                transactionOutputs.toMutableSortedListFromPreSorted(), hash
            )
        }

    override fun create(other: MutableWitness): MutableWitness =
        create(other as Witness)
}