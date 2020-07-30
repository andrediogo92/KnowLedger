package org.knowledger.ledger.adapters.storage

import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewHashList
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class MerkleTreeStorageAdapter : LedgerStorageAdapter<MutableMerkleTree> {
    override val id: String
        get() = "MerkleTree"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "nakedTree" to StorageType.LISTEMBEDDED,
            "levelIndexes" to StorageType.LISTEMBEDDED
        )

    override fun store(
        element: MutableMerkleTree, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewHashList("nakedTree", element.collapsedTree)
                pushNewNative("levelIndexes", element.levelIndex)
            }.ok()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<MutableMerkleTree, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val collapsedTree: MutableList<Hash> =
                    element.getMutableHashList("nakedTree")
                val levelIndex: MutableList<Int> =
                    element.getStorageProperty("levelIndexes")

                context.merkleTreeFactory.create(
                    context.ledgerInfo.hashers,
                    collapsedTree, levelIndex
                ).ok()
            }
        }

}