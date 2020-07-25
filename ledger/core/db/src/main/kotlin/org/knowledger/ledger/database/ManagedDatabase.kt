package org.knowledger.ledger.database

interface ManagedDatabase {
    fun newManagedSession(dbName: String): ManagedSession
    fun close()
}