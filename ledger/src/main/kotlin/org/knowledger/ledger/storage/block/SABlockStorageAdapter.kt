package org.knowledger.ledger.storage.block

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
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
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUBlockStorageAdapter,
            toStore,
            toStore.block
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlock, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUBlockStorageAdapter, ::StorageAwareBlock
        )
}