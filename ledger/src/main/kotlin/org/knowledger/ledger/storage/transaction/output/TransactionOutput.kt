package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.storage.LedgerContract
import java.security.PublicKey

interface TransactionOutput : HashSerializable, LedgerContract {
    val publicKey: PublicKey
    val previousCoinbase: Hash
    val payout: Payout
    val transactionHashes: Set<Hash>


    fun addToPayout(
        payout: Payout,
        tx: Hash,
        prev: Hash
    )
}