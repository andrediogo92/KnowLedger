package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.merkletree.SAMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.SUMerkleTreeStorageAdapter
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree

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
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareMerkleTree ->
                SAMerkleTreeStorageAdapter.store(toStore, session)
            is MerkleTreeImpl ->
                SUMerkleTreeStorageAdapter.store(toStore, session)
            else -> deadCode()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MerkleTree, LoadFailure> =
        SAMerkleTreeStorageAdapter.load(ledgerHash, element)

}