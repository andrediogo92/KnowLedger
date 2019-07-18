package org.knowledger.ledger.storage.merkletree

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageID
import org.knowledger.ledger.core.hash.Hashed
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.StorageAware
import org.knowledger.ledger.storage.simpleUpdate

internal data class StorageAwareMerkleTree(
    internal val merkleTree: StorageUnawareMerkleTree
) : MerkleTree by merkleTree, StorageAware<MerkleTree> {
    override fun update(session: NewInstanceSession): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidatedFields)

    override val invalidated: Map<String, Any>
        get() = invalidatedFields

    @Transient
    override var id: StorageID? = null

    @Transient
    private val invalidatedFields =
        mutableMapOf<String, Any>()


    internal constructor(
        hasher: Hasher
    ) : this(StorageUnawareMerkleTree(hasher))

    internal constructor(
        hasher: Hasher,
        coinbase: Hashed,
        data: Array<out Hashed>
    ) : this(hasher) {
        rebuildMerkleTree(coinbase, data)
    }

    override fun rebuildMerkleTree(data: Array<out Hashed>) {
        merkleTree.rebuildMerkleTree(data)
        if (id != null) {
            invalidatedFields["nakedTree"] = merkleTree.nakedTree
            invalidatedFields["levelIndexes"] = merkleTree.levelIndexes
        }
    }
}