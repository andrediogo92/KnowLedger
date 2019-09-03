package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.filterByIndex
import org.knowledger.ledger.service.ServiceClass

interface TransactionPool : ServiceClass {
    val transactions: List<Hash>
    val confirmations: List<Boolean>
    val unconfirmed: List<Hash>
        get() = transactions.filterByIndex {
            !confirmations[it]
        }

    val firstUnconfirmed: Hash?
        get() = unconfirmed.first()

    operator fun get(hash: Hash) =
        transactions.first {
            it == hash
        }
}