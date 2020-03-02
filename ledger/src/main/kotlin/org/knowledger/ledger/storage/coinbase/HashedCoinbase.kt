package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.serial.display.CoinbaseSerializer
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.Witness

@Serializable(with = CoinbaseSerializer::class)
interface HashedCoinbase : Hashing,
                           Coinbase,
                           Sizeable {
    fun findWitness(tx: Transaction): Int

    /**
     * Add a new output to an already known [witness].
     * The [newTransaction] is from an already known
     * identity with an already existing [witness].
     */
    fun addToWitness(
        witness: Witness,
        newIndex: Int, newTransaction: Transaction,
        latestKnownIndex: Int = -1,
        latestKnown: Transaction? = null,
        latestKnownBlockHash: Hash = Hash.emptyHash
    )

    /**
     * Add a new output to a new [Witness].
     * The [newTransaction] is from a known
     * identity with no existing [Witness]
     * in this coinbase.
     */
    fun addToWitness(
        newIndex: Int, newTransaction: Transaction,
        previousWitnessIndex: Int = -1,
        latestCoinbase: Hash = Hash.emptyHash,
        latestKnownIndex: Int = -1,
        latestKnown: Transaction? = null,
        latestKnownBlockHash: Hash = Hash.emptyHash
    )

    override fun clone(): HashedCoinbase
}