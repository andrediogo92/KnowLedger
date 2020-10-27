package org.knowledger.ledger.adapters

import org.knowledger.ledger.core.data.LedgerContract

internal interface StorageLoadable<out T : LedgerContract> : Loadable<T>