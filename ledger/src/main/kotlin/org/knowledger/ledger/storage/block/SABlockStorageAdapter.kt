package org.knowledger.ledger.storage.block

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SABlockStorageAdapter(
    private val adapterManager: AdapterManager,
    private val suBlockStorageAdapter: SUBlockStorageAdapter
) : LedgerStorageAdapter<StorageAwareBlock>,
    SchemaProvider by suBlockStorageAdapter {

    override fun store(
        toStore: StorageAwareBlock,
        session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suBlockStorageAdapter,
            toStore, toStore.block
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlock, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suBlockStorageAdapter
        ) { block ->
            StorageAwareBlock(
                adapterManager, block
            )
        }
}