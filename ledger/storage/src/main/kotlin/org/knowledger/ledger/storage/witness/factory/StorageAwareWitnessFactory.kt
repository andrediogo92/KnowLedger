package org.knowledger.ledger.storage.witness.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.witness.StorageAwareWitnessImpl
import org.knowledger.ledger.storage.witness.Witness as UnhashedWitness

@OptIn(ExperimentalSerializationApi::class)
internal class StorageAwareWitnessFactory(
    private val factory: WitnessFactory = HashedWitnessFactory(),
) : WitnessFactory {
    private fun createSA(witness: MutableWitness): StorageAwareWitnessImpl =
        StorageAwareWitnessImpl(witness)

    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        transactionOutput: TransactionOutput, hasher: Hashers, encoder: BinaryFormat,
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex, previousCoinbase, transactionOutput, hasher, encoder
        )
    )


    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat,
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex, previousCoinbase,
            payout, transactionOutputs, hasher, encoder
        )
    )

    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>, hash: Hash,
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex, previousCoinbase, payout, transactionOutputs, hash
        )
    )

    override fun create(
        witness: UnhashedWitness, hasher: Hashers, encoder: BinaryFormat,
    ): StorageAwareWitnessImpl =
        createSA(factory.create(witness, hasher, encoder))

    override fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, index: Int,
    ): StorageAwareWitnessImpl = createSA(
        factory.create(
            publicKey, previousWitnessIndex, previousCoinbase,
            payout, transactionOutputs, hash, index
        )
    )

    override fun create(witness: Witness): StorageAwareWitnessImpl =
        createSA(factory.create(witness))

    override fun create(other: MutableWitness): StorageAwareWitnessImpl =
        createSA(factory.create(other))
}