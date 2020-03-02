package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SAMerkleTreeStorageAdapter(
    private val suMerkleTreeStorageAdapter: SUMerkleTreeStorageAdapter
) : LedgerStorageAdapter<StorageAwareMerkleTree>,
    SchemaProvider by suMerkleTreeStorageAdapter {
    override fun store(
        toStore: StorageAwareMerkleTree, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            suMerkleTreeStorageAdapter,
            toStore, toStore.merkleTree
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareMerkleTree, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suMerkleTreeStorageAdapter, ::StorageAwareMerkleTree
        )
}