package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract

interface TransactionOutput : Comparable<TransactionOutput>,
                              HashSerializable, LedgerContract,
                              Hashing, Cloneable {
    val payout: Payout
    val prevTxBlock: Hash
    val prevTxIndex: Int
    val prevTx: Hash
    val txIndex: Int
    val tx: Hash

    override val hash: Hash
        get() = tx

    override fun compareTo(other: TransactionOutput): Int =
        txIndex.compareTo(other.txIndex)
}