package org.knowledger.ledger.data.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.data.MerkleTree
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter

object MerkleTreeStorageAdapter : LedgerStorageAdapter<MerkleTree> {
    override val id: String
        get() = "MerkleTree"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "collapsedTree" to StorageType.LISTEMBEDDED,
            "levelIndex" to StorageType.LISTEMBEDDED
        )

    override fun store(
        toStore: MerkleTree,
        session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashList(
                "collapsedTree", toStore.collapsedTree
            ).setStorageProperty(
                "levelIndex", toStore.levelIndex
            )


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MerkleTree, LoadFailure> =
        tryOrLoadUnknownFailure {
            val collapsedTree: List<Hash> =
                element.getHashList("collapsedTree")
            val levelIndex: List<Int> =
                element.getStorageProperty("levelIndex")
            Outcome.Ok(
                MerkleTree(
                    LedgerHandle.getHasher(ledgerHash)!!,
                    collapsedTree,
                    levelIndex
                )
            )
        }
}