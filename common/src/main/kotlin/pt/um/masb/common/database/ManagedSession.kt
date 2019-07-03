package pt.um.masb.common.database

import pt.um.masb.common.database.query.GenericQuery

interface ManagedSession : NewInstanceSession,
                           TransactionableSession {
    val isClosed: Boolean
    val managedSchemas: ManagedSchemas

    fun close(): ManagedSession
    fun makeActive(): ManagedSession
    fun reOpenIfNecessary(): ManagedSession
    fun query(query: GenericQuery): StorageResults
    fun query(query: String): StorageResults
    fun save(elem: StorageElement): StorageElement?
    fun save(elem: StorageElement, cluster: String): StorageElement?
}