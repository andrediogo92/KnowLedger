package org.knowledger.common.database

interface ManagedDatabase {
    fun newManagedSession(dbName: String): ManagedSession
    fun close()
}