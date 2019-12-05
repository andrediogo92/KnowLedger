package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter

internal object SUMerkleTreeStorageAdapter : LedgerStorageAdapter<MerkleTreeImpl> {
    override val id: String
        get() = MerkleTreeStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = MerkleTreeStorageAdapter.properties

    override fun store(
        toStore: MerkleTreeImpl, session: ManagedSession
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
    ): Outcome<MerkleTreeImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val collapsedTree: MutableList<Hash> =
                element.getMutableHashList("nakedTree")
            val levelIndex: MutableList<Int> =
                element.getStorageProperty("levelIndexes")
            LedgerHandle.getHasher(ledgerHash)?.let {
                Outcome.Ok(
                    MerkleTreeImpl(
                        collapsedTree,
                        levelIndex,
                        it
                    )
                )
            } ?: Outcome.Error(
                LoadFailure.NonMatchingHasher(ledgerHash)
            )
        }
}