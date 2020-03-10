package org.knowledger.ledger.storage.witness

import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.TransactionOutput

interface Witness : Comparable<Witness>, HashSerializable,
                    LedgerContract, Cloneable {

    val publicKey: EncodedPublicKey
    val previousWitnessIndex: Int
    val previousCoinbase: Hash
    val payout: Payout
    val transactionOutputs: SortedList<TransactionOutput>

    override fun compareTo(other: Witness): Int =
        publicKey.compareTo(other.publicKey)

    public override fun clone(): Witness
}