package org.knowledger.ledger.storage.witness

import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput

/**
 * [WitnessImpl] contains transaction hashes used
 * for calculating payout and the cumulative payout
 * for the [EncodedPublicKey] in the current containing
 * coinbase.
 */
internal data class WitnessImpl(
    override val publicKey: EncodedPublicKey,
    override val previousWitnessIndex: Int,
    override val previousCoinbase: Hash,
    override val payout: Payout,
    override val transactionOutputs: SortedList<TransactionOutput>
) : Witness {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Witness) return false

        if (publicKey != other.publicKey) return false
        if (previousWitnessIndex != other.previousWitnessIndex) return false
        if (previousCoinbase != other.previousCoinbase) return false
        if (payout != other.payout) return false
        if (transactionOutputs != other.transactionOutputs) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + previousWitnessIndex
        result = 31 * result + previousCoinbase.hashCode()
        result = 31 * result + payout.hashCode()
        result = 31 * result + transactionOutputs.hashCode()
        return result
    }


}
