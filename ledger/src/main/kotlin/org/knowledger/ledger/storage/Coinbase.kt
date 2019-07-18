package org.knowledger.ledger.storage

import org.knowledger.common.Sizeable
import org.knowledger.common.data.DataFormula
import org.knowledger.common.data.Payout
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hashable
import org.knowledger.common.hash.Hashed
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.flattenBytes
import org.knowledger.common.storage.LedgerContract
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.service.Identity

/**
 * The coinbase transaction. Pays out to contributors to
 * the ledger.
 *
 * The coinbase will be continually updated to reflect
 * changes to the block.
 */
interface Coinbase : Sizeable, Cloneable,
                     Hashed, Hashable,
                     LedgerContract {

    val payoutTXO: MutableSet<TransactionOutput>
    var payout: Payout
    val hasher: Hasher
    val formula: DataFormula
    val coinbaseParams: CoinbaseParams

    /**
     * Takes the [newTransaction] and attempts to calculate a
     * fluctuation from the [latestKnown] of the same type
     * and in the same geographical area.
     *
     * Uses the [latestUTXO] for the new [Transaction]'s publisher.
     *
     * Adds a [Payout] for the transaction's agent in a transaction
     * output.
     *
     * There may not be a [latestKnown], in which case the [newTransaction]
     * is treated as the first known [Transaction] of that type.
     *
     * There may not be a [latestUTXO], in which case the first
     * transaction output must be created for the [Identity] which
     * supplied the [newTransaction].
     */
    fun addToInput(
        newTransaction: Transaction,
        latestKnown: Transaction?,
        latestUTXO: TransactionOutput?
    )

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                payoutTXO.sumBy {
                    it.hashId.bytes.size
                },
                payoutTXO.asSequence().map {
                    it.hashId.bytes
                },
                payout.bytes
            )
        )

    public override fun clone(): Coinbase
}

