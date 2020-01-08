package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter

internal object SAMerkleTreeStorageAdapter : LedgerStorageAdapter<StorageAwareMerkleTree> {
    override val id: String
        get() = MerkleTreeStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = MerkleTreeStorageAdapter.properties

    override fun store(
        toStore: StorageAwareMerkleTree, session: ManagedSession
    ): StorageElement =
        session.cacheStore(
            SUMerkleTreeStorageAdapter,
            toStore, toStore.merkleTree
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareMerkleTree, LoadFailure> =
        element.cachedLoad(
            ledgerHash, SUMerkleTreeStorageAdapter, ::StorageAwareMerkleTree
        )
}