package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.pools.transaction.PoolTransaction
import org.knowledger.ledger.service.pools.transaction.TransactionPool
import org.knowledger.ledger.service.results.LedgerFailure

internal fun TransactionPool.persist(
    session: ManagedSession
): StorageElement =
    TransactionPoolStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadTransactionPool(
    ledgerHash: Hash
): Outcome<TransactionPool, LedgerFailure> =
    TransactionPoolStorageAdapter.load(
        ledgerHash,
        this
    )

internal fun PoolTransaction.persist(
    session: ManagedSession
): StorageElement =
    PoolTransactionStorageAdapter.persist(
        this, session
    )

internal fun StorageElement.loadPoolTransaction(
    ledgerHash: Hash
): Outcome<PoolTransaction, LedgerFailure> =
    PoolTransactionStorageAdapter.load(
        ledgerHash,
        this
    )