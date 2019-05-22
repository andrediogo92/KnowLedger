package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.byteEncodeToPublicKey
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.TransactionOutput

class TransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutput> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "prevCoinbase" to StorageType.BYTES,
            "hashId" to StorageType.BYTES,
            "payout" to StorageType.DECIMAL,
            "txSet" to StorageType.SET
        )

    override fun store(
        toStore: TransactionOutput,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            )
            setHashProperty(
                "prevCoinbase", toStore.prevCoinbase
            )
            setHashProperty("hashId", toStore.hashId)
            setPayoutProperty("payout", toStore.payout)
            setHashSet("txSet", toStore.tx)
        }

    override fun load(
        hash: Hash,
        element: StorageElement
    ): LoadResult<TransactionOutput> =
        tryOrLoadQueryFailure {
            val publicKey = byteEncodeToPublicKey(
                element.getStorageProperty("publicKey")
            )
            val prevCoinbase =
                element.getHashProperty("prevCoinbase")
            val hashId =
                element.getHashProperty("hashId")
            val payout =
                element.getPayoutProperty("payout")
            val txSet = element.getHashSet("txSet")

            LoadResult.Success(
                TransactionOutput(
                    publicKey,
                    prevCoinbase,
                    hashId,
                    payout,
                    txSet
                )
            )
        }
}