package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.HashUpdateable
import org.knowledger.ledger.storage.Markable
import org.knowledger.ledger.storage.NonceRegen

internal interface MutableHashedCoinbaseHeader : HashedCoinbaseHeader,
                                                 HashUpdateable, Markable,
                                                 NonceRegen {
    fun updatePayout(payoutToAdd: Payout)

}