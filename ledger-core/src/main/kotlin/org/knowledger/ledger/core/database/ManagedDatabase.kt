package org.knowledger.ledger.core.database

interface ManagedDatabase {
    fun newManagedSession(dbName: String): ManagedSession
    fun close()
}