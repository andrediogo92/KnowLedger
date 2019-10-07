package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Payout
import java.security.PublicKey

interface TransactionOutput : HashSerializable, LedgerContract, Cloneable {
    val publicKey: PublicKey
    val previousCoinbase: Hash
    val payout: Payout
    val transactionHashes: Set<Hash>


    fun addToPayout(
        payout: Payout,
        newTransaction: Hash,
        previousTransaction: Hash
    )

    public override fun clone(): TransactionOutput
}