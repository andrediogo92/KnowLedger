package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.coinbase.loadCoinbaseByImpl
import org.knowledger.ledger.storage.coinbase.store

object CoinbaseStorageAdapter : LedgerStorageAdapter<Coinbase> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payoutTXOs" to StorageType.SET,
            "payout" to StorageType.PAYOUT,
            "hash" to StorageType.HASH,
            "difficulty" to StorageType.DIFFICULTY,
            "blockheight" to StorageType.LONG,
            "extraNonce" to StorageType.LONG
        )

    override fun store(
        toStore: Coinbase,
        session: ManagedSession
    ): StorageElement =
        toStore.store(session)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Coinbase, LoadFailure> =
        element.loadCoinbaseByImpl(ledgerHash)

}