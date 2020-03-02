package org.knowledger.ledger.storage.coinbase

import org.knowledger.collections.SortedList
import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.data.DataFormula
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashSerializable
import org.knowledger.ledger.storage.LedgerContract
import org.knowledger.ledger.storage.Markable
import org.knowledger.ledger.storage.Witness

/**
 * The coinbase transaction. Pays out to contributors to
 * the ledger.
 *
 * The coinbase will be continually updated to reflect
 * changes to the block.
 */
interface Coinbase : Cloneable, Markable,
                     HashSerializable,
                     LedgerContract {

    val witnesses: SortedList<Witness>
    val payout: Payout
    val coinbaseParams: CoinbaseParams
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty
    val blockheight: Long
    val extraNonce: Long
    val formula: DataFormula


    public override fun clone(): Coinbase
    fun newNonce()
}

