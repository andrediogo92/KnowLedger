package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object SAChainIdStorageAdapter : ServiceStorageAdapter<StorageAwareChainId>,
                                          SchemaProvider by SUChainIdStorageAdapter {
    override fun store(
        toStore: StorageAwareChainId, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUChainIdStorageAdapter,
            toStore, toStore.chainId
        )


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareChainId, LedgerFailure> =
        element.cachedLoad(
            ledgerHash, SUChainIdStorageAdapter,
            ::StorageAwareChainId
        )
}