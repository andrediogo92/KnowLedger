package org.knowledger.ledger.storage.merkletree

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter

internal object SAMerkleTreeStorageAdapter : LedgerStorageAdapter<StorageAwareMerkleTree> {
    override val id: String
        get() = MerkleTreeStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = MerkleTreeStorageAdapter.properties

    override fun store(
        toStore: StorageAwareMerkleTree, session: NewInstanceSession
    ): StorageElement =
        session.cacheStore(
            SUMerkleTreeStorageAdapter,
            toStore, toStore.merkleTree
        )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareMerkleTree, LoadFailure> =
        element.cachedLoad(ledgerHash, SUMerkleTreeStorageAdapter) {
            StorageAwareMerkleTree(it)
        }
}