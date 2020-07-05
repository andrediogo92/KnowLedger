package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace

internal class StorageAwareBlockHeaderImpl(
    override val blockHeader: MutableHashedBlockHeader
) : MutableHashedBlockHeader by blockHeader,
    StorageAwareBlockHeader {
    override val invalidated: Array<StoragePairs<*>> =
        arrayOf(
            StoragePairs.Native("nonce"),
            StoragePairs.Hash("merkleRoot"),
            StoragePairs.Hash("hash"),
            StoragePairs.Native("seconds")
        )

    override var id: StorageID? = null

    override fun updateHash(hash: Hash) {
        blockHeader.updateHash(hash)
        invalidated.replace(2, hash)
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        simpleUpdate(invalidated)

    override fun updateMerkleTree(newRoot: Hash) {
        blockHeader.updateMerkleTree(newRoot)
        if (id != null) {
            invalidated.replace(1, newRoot)
        }
    }

    override fun newNonce() {
        blockHeader.newNonce()
        invalidated.replace(0, nonce)
    }

    override fun equals(other: Any?): Boolean =
        blockHeader == other

    override fun hashCode(): Int =
        blockHeader.hashCode()
}