package org.knowledger.ledger.storage.adapters

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
import org.knowledger.ledger.storage.Coinbase

object CoinbaseStorageAdapter : LedgerStorageAdapter<Coinbase> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payoutTXOs" to StorageType.SET,
            "coinbase" to StorageType.PAYOUT,
            "hashId" to StorageType.HASH
        )

    override fun store(
        toStore: Coinbase,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id)
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
            ).setPayoutProperty("coinbase", toStore.coinbase)
            .setHashProperty("hashId", toStore.hashId)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Coinbase, LoadFailure> =
        tryOrLoadUnknownFailure {
            element
                .getElementSet("payoutTXOs")
                .asSequence()
                .map {
                    TransactionOutputStorageAdapter.load(ledgerHash, it)
                }.allValues()
                .mapSuccess { txos ->
                    LedgerHandle.getContainer(ledgerHash)!!.let {
                        Coinbase(
                            txos.toMutableSet(),
                            element.getPayoutProperty("coinbase"),
                            element.getHashProperty("hashId"),
                            it.hasher,
                            it.formula,
                            it.coinbaseParams
                        )
                    }
                }
        }
}