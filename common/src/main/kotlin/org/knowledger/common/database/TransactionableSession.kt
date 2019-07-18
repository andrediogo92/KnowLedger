package org.knowledger.common.database

interface TransactionableSession {
    fun begin(): TransactionableSession
    fun commit(): TransactionableSession
    fun rollback(): TransactionableSession
    fun save(elem: StorageElement): StorageElement?
    fun save(elem: StorageElement, cluster: String): StorageElement?
    fun remove(id: StorageID): StorageID?
}