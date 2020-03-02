package org.knowledger.ledger.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T, U : LedgerContract> ManagedSession.cacheStore(
    storageAdapter: LedgerStorageAdapter<U>,
    toStore: T,
    inner: U
): StorageElement
        where T : StorageAware<*>,
              T : LedgerContract =
    toStore.id?.element
        ?: storageAdapter.persist(inner, this)
            .also {
                toStore.id = it.identity
            }

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T, U : ServiceClass> ManagedSession.cacheStore(
    storageAdapter: ServiceStorageAdapter<U>,
    toStore: T,
    inner: U
): StorageElement
        where T : StorageAware<*>,
              T : ServiceClass =
    toStore.id?.element
        ?: storageAdapter.persist(inner, this)
            .also {
                toStore.id = it.identity
            }


internal inline fun <T, U : LedgerContract> StorageElement.cachedLoad(
    ledgerHash: Hash,
    storageAdapter: LedgerStorageAdapter<U>,
    constructor: (U) -> T
): Outcome<T, LoadFailure>
        where T : StorageAware<*>,
              T : LedgerContract =
    storageAdapter.load(ledgerHash, this)
        .mapSuccess { u ->
            constructor(u).also {
                it.id = identity
            }
        }

internal inline fun <T, U : ServiceClass> StorageElement.cachedLoad(
    ledgerHash: Hash,
    storageAdapter: ServiceStorageAdapter<U>,
    constructor: (U) -> T
): Outcome<T, LedgerFailure>
        where T : StorageAware<*>,
              T : ServiceClass =
    storageAdapter.load(ledgerHash, this)
        .mapSuccess { u ->
            constructor(u).also {
                it.id = identity
            }
        }