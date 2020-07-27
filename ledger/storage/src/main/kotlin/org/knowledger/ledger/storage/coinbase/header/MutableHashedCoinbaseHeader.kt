package org.knowledger.ledger.storage.coinbase.header

import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.mutations.HashUpdateable
import org.knowledger.ledger.storage.mutations.Markable
import org.knowledger.ledger.storage.mutations.NonceRegen
import org.knowledger.ledger.storage.mutations.RootUpdateable

interface MutableHashedCoinbaseHeader : HashedCoinbaseHeader, HashUpdateable,
                                        RootUpdateable, Markable, NonceRegen {
    fun addToPayout(payout: Payout)

}