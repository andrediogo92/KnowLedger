package org.knowledger.ledger.storage.cache

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.err
import org.knowledger.ledger.storage.results.UpdateFailure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun StorageAware.commonUpdate(
    runUpdate: (StorageElement) -> Outcome<StorageElement, UpdateFailure>
): Outcome<StorageElement, UpdateFailure> {
    contract {
        callsInPlace(runUpdate, InvocationKind.AT_MOST_ONCE)
    }
    val id = id
    return if (id == null) {
        UpdateFailure.NotYetStored.err()
    } else {
        runUpdate(id)
    }
}

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> Array<StoragePairs<*>>.replaceUnchecked(
    index: Int, element: T
) {
    (this[index] as StoragePairs<T>).replace(element)
}

@Suppress("UNCHECKED_CAST")
internal inline fun <T, R : StorageAware> T.convertToStorageAware(
    convert: T.() -> R
): R {
    contract {
        callsInPlace(convert, InvocationKind.AT_MOST_ONCE)
    }
    return when (this) {
        is StorageAware -> this as R
        else -> this.convert()
    }
}


@Suppress("UNCHECKED_CAST")
internal fun <T, R : StorageAware> T.convertToStorageAware(): R = this as R
