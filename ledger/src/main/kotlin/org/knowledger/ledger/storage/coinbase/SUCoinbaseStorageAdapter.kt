package org.knowledger.ledger.storage.coinbase

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.allValues
import org.knowledger.common.results.mapSuccess
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