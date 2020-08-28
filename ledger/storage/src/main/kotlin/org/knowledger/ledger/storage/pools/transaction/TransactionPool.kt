package org.knowledger.ledger.storage.pools.transaction

import org.knowledger.collections.SortedList
import org.knowledger.collections.mapSorted
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.storage.ChainId
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.MutableTransaction

interface TransactionPool : LedgerContract {
    val transactions: SortedList<PoolTransaction>
    val chainId: ChainId
    val notInBlock: SortedList<MutableTransaction>
        get() = transactions.filter { !it.inBlock }.mapSorted(PoolTransaction::transaction)

    val inBlock: SortedList<MutableTransaction>
        get() = transactions.filter { it.inBlock }.mapSorted(PoolTransaction::transaction)

    val firstNotInBlock: MutableTransaction? get() = notInBlock.firstOrNull()


    fun isNotInBlock(hash: Hash): Boolean = notInBlock.any { it.hash == hash }

    fun isInBlock(hash: Hash): Boolean = inBlock.any { it.hash == hash }

    operator fun get(transaction: MutableTransaction): PoolTransaction? =
        get(transaction.hash)

    operator fun get(hash: Hash): PoolTransaction? =
        transactions.firstOrNull { it.transaction.hash == hash }
}