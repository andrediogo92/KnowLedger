package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.results.collapse
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadListResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.Coinbase

class CoinbaseStorageAdapter : LedgerStorageAdapter<Coinbase> {
    val transactionOutputStorageAdapter =
        TransactionOutputStorageAdapter()

    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "payoutTXOs" to StorageType.SET,
            "coinbase" to StorageType.DECIMAL,
            "hashId" to StorageType.BYTES
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
                        transactionOutputStorageAdapter.store(
                            it, session
                        )
                    }.toSet()
            )
            setPayoutProperty(
                "coinbase", toStore.coinbase
            )
            setHashProperty(
                "hashId", toStore.hashId
            )
        }

    override fun load(
        hash: Hash,
        element: StorageElement
    ): LoadResult<Coinbase> =
        tryOrLoadQueryFailure {
            val pTXOs =
                element
                    .getElementSet("payoutTXOs")
                    .asSequence()
                    .map {
                        transactionOutputStorageAdapter.load(hash, it)
                    }.collapse()

            if (pTXOs !is LoadListResult.Success) {
                return@tryOrLoadQueryFailure pTXOs.intoLoad<Coinbase>()
            }

            val coinbase =
                element.getPayoutProperty("coinbase")

            val hashId =
                element.getHashProperty("hashId")

            LoadResult.Success(
                Coinbase(
                    pTXOs.data.toMutableSet(),
                    coinbase,
                    hashId
                )
            )
        }
}