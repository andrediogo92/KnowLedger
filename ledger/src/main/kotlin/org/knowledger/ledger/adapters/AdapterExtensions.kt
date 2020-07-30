package org.knowledger.ledger.adapters

import com.github.michaelbull.result.onSuccess
import org.knowledger.ledger.adapters.service.ServiceStorageAdapter
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Failure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.ServiceClass
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.cache.StorageAware
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal fun <T> StorageSolver.cacheStore(
    storageAdapter: LedgerStorageAdapter<T>, element: T
): Outcome<Unit, DataFailure> where T : LedgerContract =
    (element as? StorageAware)?.id?.let {
        storageAdapter.update(element, this)
    } ?: storageAdapter.store(element, this)

internal fun <T> StorageSolver.cacheStore(
    storageAdapter: ServiceStorageAdapter<T>, element: T
): Outcome<Unit, DataFailure> where T : ServiceClass =
    (element as? StorageAware)?.id?.let {
        storageAdapter.update(element, this)
    } ?: storageAdapter.store(element, this)


internal inline fun <T, R : Failure> StorageElement.cachedLoad(
    load: () -> Outcome<T, R>
): Outcome<T, R> {
    contract {
        callsInPlace(load, InvocationKind.EXACTLY_ONCE)
    }
    return load().onSuccess { u ->
        (u as? StorageAware)?.id = this
    }
}
