package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.merkletree.SAMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.SUMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.knowledger.ledger.storage.merkletree.StorageUnawareMerkleTree

object MerkleTreeStorageAdapter : LedgerStorageAdapter<MerkleTree> {
    override val id: String
        get() = "MerkleTree"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "nakedTree" to StorageType.LISTEMBEDDED,
            "levelIndexes" to StorageType.LISTEMBEDDED
        )

    override fun store(
        toStore: MerkleTree,
        session: NewInstanceSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareMerkleTree ->
                SAMerkleTreeStorageAdapter.store(toStore, session)
            is StorageUnawareMerkleTree ->
                SUMerkleTreeStorageAdapter.store(toStore, session)
            else -> deadCode()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MerkleTree, LoadFailure> =
        SAMerkleTreeStorageAdapter.load(ledgerHash, element)

}