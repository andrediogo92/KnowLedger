package org.knowledger.ledger.storage.block

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal object SABlockStorageAdapter : LedgerStorageAdapter<StorageAwareBlock> {
    override val id: String
        get() = BlockStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = BlockStorageAdapter.properties

    override fun store(
        toStore: StorageAwareBlock,
        session: NewInstanceSession
    ): StorageElement =
        session.cacheStore(
            SUBlockStorageAdapter,
            toStore,
            toStore.block
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlock, LoadFailure> =
        element.cachedLoad(ledgerHash, SUBlockStorageAdapter) {
            StorageAwareBlock(it)
        }
}