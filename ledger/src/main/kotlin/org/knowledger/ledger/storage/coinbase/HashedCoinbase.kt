package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.hash.Hashing
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.display.CoinbaseSerializer
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput

@Serializable(with = CoinbaseSerializer::class)
interface HashedCoinbase : Hashing,
                           Coinbase,
                           Sizeable {
    /**
     * Takes the [newTransaction] and attempts to calculate a
     * fluctuation from the [latestKnown] of the same type
     * and in the same geographical area.
     *
     * Uses the [latestUTXO] for the new [HashedTransaction]'s publisher.
     *
     * Adds a [Payout] for the transaction's agent in a transaction
     * output.
     *
     * There may not be a [latestKnown], in which case the [newTransaction]
     * is treated as the first known [HashedTransaction] of that type.
     *
     * There may not be a [latestUTXO], in which case the first
     * transaction output must be created for the [Identity] which
     * supplied the [newTransaction].
     */
    fun addToInput(
        newTransaction: HashedTransaction,
        latestKnown: HashedTransaction?,
        latestUTXO: HashedTransactionOutput?
    )

    fun newNonce()

    override fun clone(): HashedCoinbase
}