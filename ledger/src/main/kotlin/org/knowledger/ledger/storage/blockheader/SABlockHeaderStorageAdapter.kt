package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SABlockHeaderStorageAdapter : LedgerStorageAdapter<StorageAwareBlockHeader> {
    override val id: String
        get() = BlockHeaderStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = BlockHeaderStorageAdapter.properties

    override fun store(
        toStore: StorageAwareBlockHeader, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUBlockHeaderStorageAdapter,
            toStore, toStore.blockHeader
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlockHeader, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUBlockHeaderStorageAdapter, ::StorageAwareBlockHeader
        )

}