package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.config.adapters.LedgerIdStorageAdapter
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

class LedgerHandleStorageAdapter : ServiceLoadable<LedgerHandle>,
                                   Storable<LedgerHandle> {
    val ledgerIdStorageAdapter = LedgerIdStorageAdapter()

    override fun store(
        toStore: LedgerHandle, session: NewInstanceSession
    ): StorageElement =
        ledgerIdStorageAdapter.store(toStore.ledgerId, session)

    override fun load(
        persistenceWrapper: PersistenceWrapper,
        hash: Hash,
        element: StorageElement
    ): LedgerResult<LedgerHandle> =
        ledgerIdStorageAdapter.load(
            hash,
            element
        ).intoLedger {
            LedgerHandle(
                persistenceWrapper, this
            )
        }
}