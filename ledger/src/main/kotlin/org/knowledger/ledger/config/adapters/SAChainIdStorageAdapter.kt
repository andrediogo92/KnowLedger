package org.knowledger.ledger.config.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.config.StorageAwareChainId
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
        toStore.id?.element ?: SUChainIdStorageAdapter
            .store(toStore.chainId, session)
            .also { toStore.id = it.identity }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<StorageAwareChainId, LedgerFailure> =
        SUChainIdStorageAdapter
            .load(ledgerHash, element)
            .mapSuccess { chainId ->
                StorageAwareChainId(chainId).also {
                    it.id = element.identity
                }
            }
}