package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.merkletree.loadMerkleTreeByImpl
import org.knowledger.ledger.storage.merkletree.store

internal object MerkleTreeStorageAdapter : LedgerStorageAdapter<MerkleTree> {
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
        toStore.store(session)


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MerkleTree, LoadFailure> =
        element.loadMerkleTreeByImpl(ledgerHash)

}