package pt.um.masb.ledger.data.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter

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
        session.newInstance(id).apply {
            setHashList(
                "collapsedTree", toStore.collapsedTree
            )
            setStorageProperty(
                "levelIndex", toStore.levelIndex
            )
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<MerkleTree, LoadFailure> =
        tryOrLoadUnknownFailure {
            val collapsedTree: List<Hash> =
                element.getHashList("collapsedTree")
            val levelIndex: List<Int> =
                element.getStorageProperty("levelIndex")
            Outcome.Ok<MerkleTree, LoadFailure>(
                MerkleTree(
                    LedgerHandle.getHasher(ledgerHash)!!,
                    collapsedTree,
                    levelIndex
                )
            )
        }
}