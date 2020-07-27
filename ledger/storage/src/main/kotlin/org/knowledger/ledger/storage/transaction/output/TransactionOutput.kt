package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.core.data.LedgerContract
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing

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