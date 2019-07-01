package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.ServiceClass
import pt.um.masb.ledger.service.results.LedgerFailure

interface ServiceLoadable<T : ServiceClass> {
    fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<T, LedgerFailure>
}