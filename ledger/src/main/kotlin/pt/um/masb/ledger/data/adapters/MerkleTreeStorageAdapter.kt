package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter

class MerkleTreeStorageAdapter : LedgerStorageAdapter<MerkleTree> {
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
        session.newInstance(id).apply {
            setHashList(
                "collapsedTree", toStore.collapsedTree
            )
            setStorageProperty(
                "levelIndex", toStore.levelIndex
            )
        }


    override fun load(
        hash: Hash,
        element: StorageElement
    ): LoadResult<MerkleTree> =
        tryOrLoadQueryFailure {
            val collapsedTree: List<Hash> =
                element.getHashList("collapsedTree")
            val levelIndex: List<Int> =
                element.getStorageProperty("levelIndex")
            LoadResult.Success(
                MerkleTree(
                    collapsedTree,
                    levelIndex
                )
            )
        }
}