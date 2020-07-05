package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageID
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.service.results.UpdateFailure
import org.knowledger.ledger.storage.StoragePairs
import org.knowledger.ledger.storage.replace

internal class StorageAwareCoinbaseHeaderImpl(
    override val coinbaseHeader: MutableHashedCoinbaseHeader,
    override val invalidated: Array<StoragePairs<*>>
) : MutableHashedCoinbaseHeader by coinbaseHeader,
    StorageAwareCoinbaseHeader {
    override var id: StorageID? = null

    override fun newNonce() {
        coinbaseHeader.newNonce()
        if (id != null) {
            invalidated.replace(0, extraNonce)
        }
    }

    override fun markForMining(blockheight: Long, difficulty: Difficulty) {
        coinbaseHeader.markForMining(blockheight, difficulty)
        if (id != null) {
            invalidated.replace(3, blockheight)
            invalidated.replace(4, difficulty)
        }
    }

    override fun update(
        session: ManagedSession
    ): Outcome<StorageID, UpdateFailure> =
        updateLinked(session, invalidated)


    override fun equals(other: Any?): Boolean =
        coinbaseHeader == other

    override fun hashCode(): Int =
        coinbaseHeader.hashCode()
}