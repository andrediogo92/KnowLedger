package org.knowledger.ledger.service.pools.transaction

import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.service.ServiceClass

interface TransactionPool : ServiceClass {
    val transactions: Set<PoolTransaction>
    val unconfirmed: List<StorageID>
        get() = transactions.filter {
            !it.confirmed
        }.map {
            it.id
        }

    val firstUnconfirmed: StorageID?
        get() = unconfirmed.firstOrNull()

    operator fun get(id: StorageID) =
        transactions.firstOrNull {
            it.id == id
        }

}