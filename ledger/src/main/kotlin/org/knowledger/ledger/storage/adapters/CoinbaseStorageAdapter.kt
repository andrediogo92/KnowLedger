package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.coinbase.SACoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.SUHCoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase

object CoinbaseStorageAdapter : LedgerStorageAdapter<HashedCoinbase> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payoutTXOs" to StorageType.SET,
            "payout" to StorageType.PAYOUT,
            "hash" to StorageType.HASH,
            "difficulty" to StorageType.DIFFICULTY,
            "blockheight" to StorageType.LONG
        )

    override fun store(
        toStore: HashedCoinbase,
        session: NewInstanceSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareCoinbase ->
                SACoinbaseStorageAdapter.store(toStore, session)
            is HashedCoinbaseImpl ->
                SUHCoinbaseStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedCoinbase, LoadFailure> =
        SACoinbaseStorageAdapter.load(ledgerHash, element)

}