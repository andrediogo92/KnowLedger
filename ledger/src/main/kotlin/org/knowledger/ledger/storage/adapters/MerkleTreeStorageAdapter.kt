package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.merkletree.SAMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.SUMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree

internal class MerkleTreeStorageAdapter(
    private val suMerkleTreeStorageAdapter: SUMerkleTreeStorageAdapter,
    private val saMerkleTreeStorageAdapter: SAMerkleTreeStorageAdapter
) : LedgerStorageAdapter<MerkleTree> {
    override val id: String
        get() = suMerkleTreeStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = suMerkleTreeStorageAdapter.properties

    override fun store(
        toStore: MerkleTree,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareMerkleTree ->
                saMerkleTreeStorageAdapter.store(toStore, session)
            is MerkleTreeImpl ->
                suMerkleTreeStorageAdapter.store(toStore, session)
            else -> deadCode()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MerkleTree, LoadFailure> =
        saMerkleTreeStorageAdapter.load(ledgerHash, element)

}