package org.knowledger.ledger.config.chainid

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object SAChainIdStorageAdapter : ServiceStorageAdapter<StorageAwareChainId> {
    override val id: String
        get() = ChainIdStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = ChainIdStorageAdapter.properties

    override fun store(
        toStore: StorageAwareChainId, session: NewInstanceSession
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