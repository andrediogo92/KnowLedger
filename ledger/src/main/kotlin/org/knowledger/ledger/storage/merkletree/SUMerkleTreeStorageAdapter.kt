package org.knowledger.ledger.storage.merkletree

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter

object SUMerkleTreeStorageAdapter : LedgerStorageAdapter<StorageUnawareMerkleTree> {
    override val id: String
        get() = MerkleTreeStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = MerkleTreeStorageAdapter.properties

    override fun store(
        toStore: StorageUnawareMerkleTree, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(MerkleTreeStorageAdapter.id)
            .setHashList(
                "nakedTree", toStore.collapsedTree
            ).setStorageProperty(
                "levelIndexes", toStore.levelIndex
            )

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageUnawareMerkleTree, LoadFailure> =
        tryOrLoadUnknownFailure {
            val collapsedTree: MutableList<Hash> =
                element.getMutableHashList("nakedTree")
            val levelIndex: MutableList<Int> =
                element.getStorageProperty("levelIndexes")
            Outcome.Ok(
                StorageUnawareMerkleTree(
                    LedgerHandle.getHasher(ledgerHash)!!,
                    collapsedTree,
                    levelIndex
                )
            )
        }
}