package org.knowledger.ledger.storage.coinbase

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.allValues
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter

object SUCoinbaseStorageAdapter : LedgerStorageAdapter<StorageUnawareCoinbase> {
    override val id: String
        get() = CoinbaseStorageAdapter.id
    override val properties: Map<String, StorageType>
        get() = CoinbaseStorageAdapter.properties

    override fun store(
        toStore: StorageUnawareCoinbase, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(CoinbaseStorageAdapter.id)
            .setElementSet(
                "payoutTXOs",
                toStore
                    .payoutTXO
                    .asSequence()
                    .map {
                        TransactionOutputStorageAdapter.store(
                            it, session
                        )
                    }.toSet()
            ).setPayoutProperty("payout", toStore.payout)
            .setHashProperty("hashId", toStore.hashId)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageUnawareCoinbase, LoadFailure> =
        tryOrLoadUnknownFailure {
            element
                .getElementSet("payoutTXOs")
                .asSequence()
                .map {
                    TransactionOutputStorageAdapter.load(ledgerHash, it)
                }.allValues()
                .mapSuccess { txos ->
                    LedgerHandle.getContainer(ledgerHash)!!.let {
                        StorageUnawareCoinbase(
                            txos.toMutableSet(),
                            element.getPayoutProperty("payout"),
                            element.getHashProperty("hashId"),
                            it.hasher,
                            it.formula,
                            it.coinbaseParams
                        )
                    }
                }
        }
}