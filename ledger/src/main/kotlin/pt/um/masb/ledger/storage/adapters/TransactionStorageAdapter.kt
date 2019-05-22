package pt.um.masb.ledger.storage.adapters


import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.byteEncodeToPublicKey
import pt.um.masb.ledger.data.adapters.PhysicalDataStorageAdapter
import pt.um.masb.ledger.results.intoLoad
import pt.um.masb.ledger.results.tryOrLoadQueryFailure
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.Transaction

class TransactionStorageAdapter : LedgerStorageAdapter<Transaction> {
    val physicalDataStorageAdapter = PhysicalDataStorageAdapter()

    override val id: String
        get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "data" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hashId" to StorageType.BYTES
        )

    override fun store(
        toStore: Transaction,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            )
            setLinked(
                "data", physicalDataStorageAdapter,
                toStore.data, session
            )
            setStorageProperty(
                "signature",
                session.newInstance(
                    toStore.signature
                )
            )
            setHashProperty(
                "hashId", toStore.hashId
            )
        }

    override fun load(
        hash: Hash,
        element: StorageElement
    ): LoadResult<Transaction> =
        tryOrLoadQueryFailure {
            val publicKey = byteEncodeToPublicKey(
                element.getStorageProperty("publicKey")
            )

            val data = physicalDataStorageAdapter.load(
                hash,
                element.getLinked("data")
            )

            if (data !is LoadResult.Success) {
                return@tryOrLoadQueryFailure data.intoLoad<Transaction>()
            }

            val signature =
                element.getStorageBytes("signature").bytes

            LoadResult.Success(
                Transaction(
                    publicKey,
                    data.data,
                    signature
                )
            )
        }
}