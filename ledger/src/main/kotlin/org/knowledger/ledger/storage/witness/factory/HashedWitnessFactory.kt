package org.knowledger.ledger.storage.witness.factory

import kotlinx.serialization.BinaryFormat
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.service.CloningFactory
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.witness.HashedWitness
import org.knowledger.ledger.storage.witness.Witness

internal interface HashedWitnessFactory :
    CloningFactory<MutableWitness> {
    fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash,
        transactionOutput: TransactionOutput,
        hasher: Hashers, encoder: BinaryFormat
    ): MutableWitness

    fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash, index: Int
    ): MutableWitness

    fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hasher: Hashers, encoder: BinaryFormat
    ): MutableWitness

    fun create(
        publicKey: EncodedPublicKey,
        previousWitnessIndex: Int,
        previousCoinbase: Hash, payout: Payout,
        transactionOutputs: MutableSortedList<TransactionOutput>,
        hash: Hash
    ): MutableWitness

    fun create(
        witness: Witness,
        hasher: Hashers, encoder: BinaryFormat
    ): MutableWitness

    fun create(
        witness: HashedWitness
    ): MutableWitness
}