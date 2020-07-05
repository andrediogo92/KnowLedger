package org.knowledger.ledger.storage.witness

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput

internal data class HashedWitnessImpl(
    override val publicKey: EncodedPublicKey,
    override val previousWitnessIndex: Int,
    override val previousCoinbase: Hash,
    private var _payout: Payout,
    private val _transactionOutputs: MutableSortedList<TransactionOutput>,
    private var _hash: Hash,
    private var _index: Int = -1
) : MutableHashedWitness {
    override val transactionOutputs: SortedList<TransactionOutput>
        get() = _transactionOutputs

    override val payout: Payout
        get() = _payout

    override val hash
        get() = _hash

    override val index: Int
        get() = _index

    override fun addToPayout(
        transactionOutput: TransactionOutput
    ) {
        _transactionOutputs.add(transactionOutput)
        _payout += transactionOutput.payout
    }

    override fun updateHash(hash: Hash) {
        _hash = hash
    }

    override fun markIndex(index: Int) {
        _index = index
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HashedWitness) return false

        if (publicKey != other.publicKey) return false
        if (previousWitnessIndex != other.previousWitnessIndex) return false
        if (previousCoinbase != other.previousCoinbase) return false
        if (_payout != other.payout) return false
        if (_transactionOutputs != other.transactionOutputs) return false
        if (_hash != other.hash) return false
        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + previousWitnessIndex
        result = 31 * result + previousCoinbase.hashCode()
        result = 31 * result + _payout.hashCode()
        result = 31 * result + _transactionOutputs.hashCode()
        result = 31 * result + _hash.hashCode()
        return result
    }


}