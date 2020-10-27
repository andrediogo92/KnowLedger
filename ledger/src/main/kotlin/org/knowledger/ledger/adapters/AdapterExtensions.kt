package org.knowledger.ledger.adapters

import com.github.michaelbull.result.onSuccess
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Failure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.storage.cache.StorageAware
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


internal inline fun <T, R : Failure> StorageElement.cachedLoad(
    load: StorageElement.() -> Outcome<T, R>,
): Outcome<T, R> {
    contract {
        callsInPlace(load, InvocationKind.EXACTLY_ONCE)
    }
    return load().onSuccess { (it as? StorageAware)?.id = this }
}
