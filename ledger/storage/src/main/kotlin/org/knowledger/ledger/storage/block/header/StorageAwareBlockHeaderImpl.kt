package org.knowledger.ledger.storage.block.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal class StorageAwareBlockHeaderImpl(
    override val blockHeader: MutableHashedBlockHeader,
) : MutableHashedBlockHeader by blockHeader, StorageAwareBlockHeader {
    override val lock: BooleanLocking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.LinkedHash("hash"),
        StoragePairs.LinkedHash("merkleRoot"),
        StoragePairs.Native("nonce"),
        StoragePairs.Native("seconds")
    )

    override fun updateHash(hash: Hash) {
        blockHeader.updateHash(hash)
        invalidated.replaceUnchecked(0, hash)
    }

    override fun updateMerkleRoot(merkleRoot: Hash) {
        blockHeader.updateMerkleRoot(merkleRoot)
        if (id != null) {
            invalidated.replaceUnchecked(1, merkleRoot)
        }
    }

    override fun newNonce() {
        blockHeader.newNonce()
        if (id != null) {
            invalidated.replaceUnchecked(2, nonce)
        }
    }

    override fun equals(other: Any?): Boolean =
        blockHeader == other

    override fun hashCode(): Int =
        blockHeader.hashCode()
}