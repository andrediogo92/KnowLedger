package org.knowledger.ledger.storage.witness.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import org.knowledger.ledger.storage.witness.Witness as UnhashedWitness

@OptIn(ExperimentalSerializationApi::class)
interface WitnessFactory : CloningFactory<MutableWitness> {
    fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        transactionOutput: TransactionOutput, hasher: Hashers, encoder: BinaryFormat,
    ): MutableWitness

    fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, index: Int,
    ): MutableWitness

    fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat,
    ): MutableWitness

    fun create(
        publicKey: EncodedPublicKey, previousWitnessIndex: Int, previousCoinbase: Hash,
        payout: Payout, transactionOutputs: MutableSortedList<TransactionOutput>, hash: Hash,
    ): MutableWitness

    fun create(witness: UnhashedWitness, hasher: Hashers, encoder: BinaryFormat): MutableWitness

    fun create(witness: Witness): MutableWitness
}