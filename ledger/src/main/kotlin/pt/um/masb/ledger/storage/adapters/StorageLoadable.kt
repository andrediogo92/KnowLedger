package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.ledger.service.results.LoadFailure

interface StorageLoadable<T : LedgerContract> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LoadFailure>
}