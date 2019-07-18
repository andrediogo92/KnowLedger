package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.coinbase.SACoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.SUCoinbaseStorageAdapter
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.coinbase.StorageUnawareCoinbase

object CoinbaseStorageAdapter : LedgerStorageAdapter<Coinbase> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payoutTXOs" to StorageType.SET,
            "payout" to StorageType.PAYOUT,
            "hashId" to StorageType.HASH
        )

    override fun store(
        toStore: Coinbase,
        session: NewInstanceSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareCoinbase ->
                SACoinbaseStorageAdapter.store(toStore, session)
            is StorageUnawareCoinbase ->
                SUCoinbaseStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Coinbase, LoadFailure> =
        SACoinbaseStorageAdapter.load(ledgerHash, element)

}