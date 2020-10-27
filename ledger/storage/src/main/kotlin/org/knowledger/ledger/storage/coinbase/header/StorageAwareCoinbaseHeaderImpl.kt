package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.cache.BooleanLocking
import org.knowledger.ledger.storage.cache.Locking
import org.knowledger.ledger.storage.cache.StoragePairs
import org.knowledger.ledger.storage.cache.replaceUnchecked

internal class StorageAwareCoinbaseHeaderImpl(
    override val coinbaseHeader: MutableHashedCoinbaseHeader,
) : MutableHashedCoinbaseHeader by coinbaseHeader, StorageAwareCoinbaseHeader {
    override val lock: Locking = BooleanLocking()
    override var id: StorageElement? = null
    override val invalidated: Array<StoragePairs<*>> = arrayOf(
        StoragePairs.LinkedHash("hash"),
        StoragePairs.LinkedHash("merkleRoot"),
        StoragePairs.Native("payout"),
        StoragePairs.Native("blockheight"),
        StoragePairs.LinkedDifficulty("difficulty"),
        StoragePairs.Native("extraNonce")
    )

    override fun addToPayout(payout: Payout) {
        coinbaseHeader.addToPayout(payout)
        if (id != null) {
            invalidated.replaceUnchecked(2, payout)
        }
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        coinbaseHeader.markForMining(blockheight, difficulty)
        if (id != null) {
            invalidated.replaceUnchecked(3, blockheight)
            invalidated.replaceUnchecked(4, difficulty)
        }
    }

    override fun newNonce() {
        coinbaseHeader.newNonce()
        if (id != null) {
            invalidated.replaceUnchecked(5, extraNonce)
        }
    }

    override fun updateHash(hash: Hash) {
        coinbaseHeader.updateHash(hash)
        if (id != null) {
            invalidated.replaceUnchecked(0, hash)
        }
    }

    override fun updateMerkleRoot(merkleRoot: Hash) {
        coinbaseHeader.updateMerkleRoot(merkleRoot)
        if (id != null) {
            invalidated.replaceUnchecked(1, merkleRoot)
        }
    }

    override fun equals(other: Any?): Boolean = coinbaseHeader == other

    override fun hashCode(): Int = coinbaseHeader.hashCode()
}