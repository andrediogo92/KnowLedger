package org.knowledger.ledger.storage.coinbase

import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.base.Sizeable
import org.knowledger.ledger.crypto.Hashing
import org.knowledger.ledger.serial.display.CoinbaseSerializer
import org.knowledger.ledger.storage.Transaction

@Serializable(with = CoinbaseSerializer::class)
interface HashedCoinbase : Hashing,
                           Coinbase,
                           Sizeable {
    fun findWitness(tx: Transaction): Int



    override fun clone(): HashedCoinbase
}