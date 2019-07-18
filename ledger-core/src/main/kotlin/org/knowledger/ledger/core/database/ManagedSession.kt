package org.knowledger.ledger.core.database

import org.knowledger.ledger.core.database.query.GenericQuery

interface ManagedSession : NewInstanceSession,
                           TransactionableSession {
    val isClosed: Boolean
    val managedSchemas: ManagedSchemas

    fun close(): ManagedSession
    fun makeActive(): ManagedSession
    fun reOpenIfNecessary(): ManagedSession
    fun query(query: GenericQuery): StorageResults
    fun query(query: String): StorageResults
}