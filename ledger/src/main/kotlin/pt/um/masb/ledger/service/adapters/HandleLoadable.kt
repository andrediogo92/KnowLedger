package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.LedgerConfig
import pt.um.masb.ledger.service.handles.LedgerHandle

interface HandleLoadable {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure>
}