package org.knowledger.ledger.storage.cache

interface Locking {

    val state: LockState
    fun lock()
    fun release()
}