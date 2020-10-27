package org.knowledger.ledger.adapters.storage

import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.results.LoadFailure

internal class MerkleTreeStorageAdapter : LedgerStorageAdapter<MutableMerkleTree> {
    override val id: String get() = "MerkleTree"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "nakedTree" to StorageType.LISTEMBEDDED,
            "levelIndexes" to StorageType.LISTEMBEDDED,
        )

    override fun store(
        element: MutableMerkleTree, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewHashList("nakedTree", element.collapsedTree)
            pushNewNative("levelIndexes", element.levelIndex)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableMerkleTree, LoadFailure> =
        element.cachedLoad {
            val collapsedTree: MutableList<Hash> =
                getMutableHashList("nakedTree")
            val levelIndex: MutableList<Int> =
                getStorageProperty("levelIndexes")

            context.merkleTreeFactory.create(
                context.ledgerInfo.hashers, collapsedTree, levelIndex,
            ).ok()
        }

}