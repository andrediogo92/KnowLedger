package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.SAChainIdStorageAdapter
import org.knowledger.ledger.config.chainid.SUChainIdStorageAdapter
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.config.chainid.StorageUnawareChainId
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object ChainIdStorageAdapter : ServiceStorageAdapter<ChainId> {
    override val id: String
        get() = "ChainId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.HASH,
            "ledgerHash" to StorageType.HASH,
            "hash" to StorageType.HASH
        )

    override fun store(
        toStore: ChainId, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareChainId ->
                SAChainIdStorageAdapter.store(toStore, session)
            is StorageUnawareChainId ->
                SUChainIdStorageAdapter.store(toStore, session)
            else ->
                deadCode()
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainId, LedgerFailure> =
        SAChainIdStorageAdapter.load(ledgerHash, element)
}