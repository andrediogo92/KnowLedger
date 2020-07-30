package org.knowledger.ledger.adapters

import org.knowledger.ledger.core.data.LedgerContract
import org.knowledger.ledger.storage.results.LoadFailure

internal interface StorageLoadable<out T : LedgerContract> : Loadable<T, LoadFailure>