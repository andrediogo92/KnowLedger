package org.knowledger.ledger.storage.blockheader

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SABlockHeaderStorageAdapter : LedgerStorageAdapter<StorageAwareBlockHeader> {
    override val id: String
        get() = BlockHeaderStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = BlockHeaderStorageAdapter.properties

    override fun store(
        toStore: StorageAwareBlockHeader, session: NewInstanceSession
    ): StorageElement =
        session.cacheStore(
            SUBlockHeaderStorageAdapter,
            toStore, toStore.blockHeader
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlockHeader, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUBlockHeaderStorageAdapter
        ) {
            StorageAwareBlockHeader(it)
        }

}