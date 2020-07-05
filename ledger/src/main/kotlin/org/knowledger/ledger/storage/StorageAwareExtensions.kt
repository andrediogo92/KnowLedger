package org.knowledger.ledger.storage

import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

internal inline fun StorageAware.commonUpdate(
    runUpdate: (StorageElement) -> Outcome<StorageID, UpdateFailure>
): Outcome<StorageID, UpdateFailure> {
    contract {
        callsInPlace(runUpdate, InvocationKind.AT_MOST_ONCE)
    }
    return if (id == null) {
        Outcome.Error(
            UpdateFailure.NotYetStored
        )
    } else {
        runUpdate(id!!.element)
    }
}


@Suppress("UNCHECKED_CAST")
internal fun <T> Array<StoragePairs<*>>.replace(
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


