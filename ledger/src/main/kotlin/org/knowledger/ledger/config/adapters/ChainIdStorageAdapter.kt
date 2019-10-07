package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.loadChainIdByImpl
import org.knowledger.ledger.config.chainid.store
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object ChainIdStorageAdapter : ServiceStorageAdapter<ChainId> {
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
        toStore.store(session)


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainId, LedgerFailure> =
        element.loadChainIdByImpl(ledgerHash)
}