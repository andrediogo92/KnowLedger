package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

internal class SUMerkleTreeStorageAdapter(
    private val hasher: Hashers,
    private val merkleTreeFactory: MerkleTreeFactory
) : LedgerStorageAdapter<MutableMerkleTree> {
    override val id: String
        get() = "MerkleTree"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "nakedTree" to StorageType.LISTEMBEDDED,
            "levelIndexes" to StorageType.LISTEMBEDDED
        )

    override fun store(
        toStore: MutableMerkleTree, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashList(
                "nakedTree", toStore.collapsedTree
            ).setStorageProperty(
                "levelIndexes", toStore.levelIndex
            )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MutableMerkleTree, LoadFailure> =
        tryOrLoadUnknownFailure {
            val collapsedTree: MutableList<Hash> =
                element.getMutableHashList("nakedTree")
            val levelIndex: MutableList<Int> =
                element.getStorageProperty("levelIndexes")

            Outcome.Ok(
                merkleTreeFactory.create(
                    hasher = hasher,
                    collapsedTree = collapsedTree,
                    levelIndex = levelIndex
                )
            )
        }
}