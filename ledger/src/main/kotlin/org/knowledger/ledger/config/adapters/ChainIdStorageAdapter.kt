package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.SUChainIdStorageAdapter
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.config.chainid.factory.StorageAwareChainIdFactory
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal class ChainIdStorageAdapter(
    chainIdFactory: StorageAwareChainIdFactory
) : ServiceStorageAdapter<ChainId> {
    private val suChainIdStorageAdapter: ServiceStorageAdapter<ChainId> =
        SUChainIdStorageAdapter(chainIdFactory)

    override val id: String
        get() = suChainIdStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suChainIdStorageAdapter.properties


    override fun store(
        toStore: ChainId, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareChainId -> session.cacheStore(
                suChainIdStorageAdapter, toStore,
                toStore.chainId
            )
            else -> suChainIdStorageAdapter.store(
                toStore, session
            )
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareChainId, LedgerFailure> =
        element.cachedLoad(
            ledgerHash, suChainIdStorageAdapter
        )
}