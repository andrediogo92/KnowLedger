package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.config.CoinbaseParams
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.core.storage.LedgerContract
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput

/**
 * The coinbase transaction. Pays out to contributors to
 * the ledger.
 *
 * The coinbase will be continually updated to reflect
 * changes to the block.
 */
interface Coinbase : Cloneable,
                     HashSerializable,
                     LedgerContract {

    val transactionOutputs: Set<HashedTransactionOutput>
    var payout: Payout
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty
    var blockheight: Long
    val formula: DataFormula
    val coinbaseParams: CoinbaseParams

    public override fun clone(): Coinbase
}

