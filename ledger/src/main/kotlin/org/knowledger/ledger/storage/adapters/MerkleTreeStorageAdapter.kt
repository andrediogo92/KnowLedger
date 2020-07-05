package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.adapters.cacheStore
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.merkletree.SUMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTreeFactory

internal class MerkleTreeStorageAdapter(
    hasher: Hashers,
    merkleTreeFactory: StorageAwareMerkleTreeFactory
) : LedgerStorageAdapter<MutableMerkleTree> {
    private val suMerkleTreeStorageAdapter: SUMerkleTreeStorageAdapter =
        SUMerkleTreeStorageAdapter(hasher, merkleTreeFactory)

    override val id: String
        get() = suMerkleTreeStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = suMerkleTreeStorageAdapter.properties

    override fun store(
        toStore: MutableMerkleTree, session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareMerkleTree -> session.cacheStore(
                suMerkleTreeStorageAdapter,
                toStore, toStore.merkleTree
            )
            else -> suMerkleTreeStorageAdapter.store(
                toStore, session
            )
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareMerkleTree, LoadFailure> =
        element.cachedLoad(
            ledgerHash, suMerkleTreeStorageAdapter
        )

}