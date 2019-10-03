package org.knowledger.ledger.storage.merkletree

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hashing
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.addOrReplaceInstances
import org.knowledger.ledger.storage.simpleUpdate

@Serializable
@SerialName("StorageMerkleTreeWrapper")
internal data class StorageAwareMerkleTree(
    internal val merkleTree: MerkleTreeImpl
) : MerkleTree by merkleTree, StorageAware<MerkleTree> {
    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override val invalidated: List<StoragePairs>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    private val invalidatedFields =
        mutableListOf<StoragePairs>()


    internal constructor(
        hasher: Hashers
    ) : this(
        merkleTree = MerkleTreeImpl(hasher = hasher)
    )

    internal constructor(
        hasher: Hashers,
        coinbase: Hashing,
        data: Array<out Hashing>
    ) : this(hasher = hasher) {
        rebuildMerkleTree(
            coinbase = coinbase, data = data
        )
    }

    override fun rebuildMerkleTree(data: Array<out Hashing>) {
        merkleTree.rebuildMerkleTree(data)
        if (id != null) {
            invalidatedFields.addOrReplaceInstances(
                arrayOf(
                    "nakedTree",
                    "levelIndexes"
                ),
                arrayOf(
                    StoragePairs.Element.HashList(merkleTree.collapsedTree),
                    StoragePairs.Element.Native(merkleTree.levelIndex)
                )
            )
        }
    }
}