package org.knowledger.ledger.storage.blockheader

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SABlockHeaderStorageAdapter(
    private val suBlockHeaderStorageAdapter: SUBlockHeaderStorageAdapter
) : LedgerStorageAdapter<StorageAwareBlockHeader>,
    SchemaProvider by suBlockHeaderStorageAdapter {
    override fun store(
        toStore: StorageAwareBlockHeader, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suBlockHeaderStorageAdapter,
            toStore, toStore.blockHeader
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlockHeader, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suBlockHeaderStorageAdapter, ::StorageAwareBlockHeader
        )

}