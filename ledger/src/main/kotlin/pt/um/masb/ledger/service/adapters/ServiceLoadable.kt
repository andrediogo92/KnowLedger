package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.service.ServiceHandle
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

interface ServiceLoadable<T : ServiceHandle> {
    fun load(
        persistenceWrapper: PersistenceWrapper,
        hash: Hash,
        element: StorageElement
    ): LedgerResult<T>
}