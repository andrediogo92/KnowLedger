package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object SAChainIdStorageAdapter : ServiceStorageAdapter<StorageAwareChainId> {
    override val id: String
        get() = ChainIdStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = ChainIdStorageAdapter.properties

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
        element.cachedLoad(ledgerHash, SUChainIdStorageAdapter) {
            StorageAwareChainId(it)
        }
}