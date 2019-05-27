package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

interface ServiceLoadable<T : ServiceHandle> {
    fun load(
        persistenceWrapper: PersistenceWrapper,
        element: StorageElement
    ): Outcome<T, LedgerFailure>
}