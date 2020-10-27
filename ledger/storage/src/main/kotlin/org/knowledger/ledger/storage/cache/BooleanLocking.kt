package org.knowledger.ledger.storage.cache

data class BooleanLocking(private var lock: LockState = LockState.Unlocked) : Locking {
    override val state: LockState get() = lock
    override fun lock() {
        lock = LockState.Locked
    }

    override fun release() {
        lock = LockState.Unlocked
    }
}