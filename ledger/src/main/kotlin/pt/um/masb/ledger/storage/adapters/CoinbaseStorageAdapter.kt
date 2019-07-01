package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.allValues
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.handles.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Coinbase

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
        session.newInstance(id).apply {
            setElementSet(
                "payoutTXOs",
                toStore
                    .payoutTXO
                    .asSequence()
                    .map {
                        TransactionOutputStorageAdapter.store(
                            it, session
                        )
                    }.toSet()
            )
            setPayoutProperty("coinbase", toStore.coinbase)
            setHashProperty("hashId", toStore.hashId)
        }

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