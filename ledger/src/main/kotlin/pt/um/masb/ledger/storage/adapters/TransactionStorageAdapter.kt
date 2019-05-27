package pt.um.masb.ledger.storage.adapters


import mu.KLogging
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.byteEncodeToPublicKey
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.data.adapters.PhysicalDataStorageAdapter
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.Transaction

object TransactionStorageAdapter : KLogging(), LedgerStorageAdapter<Transaction> {
    override val id: String
        get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "data" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hashId" to StorageType.HASH
        )

    override fun store(
        toStore: Transaction,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            this
                .setStorageProperty("publicKey", toStore.publicKey.encoded)
                .setLinked(
                    "data", PhysicalDataStorageAdapter,
                    toStore.data, session
                ).setStorageBytes(
                    "signature",
                    session.newInstance(
                        toStore.signature
                    )
                ).setHashProperty("hashId", toStore.hashId)
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<Transaction, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKey = byteEncodeToPublicKey(
                element.getStorageProperty("publicKey")
            )

            PhysicalDataStorageAdapter.load(
                ledgerHash,
                element.getLinked("data")
            ).flatMapSuccess {
                val signature =
                    element.getStorageBytes("signature").bytes

                val hash =
                    element.getHashProperty("hashId")

                Transaction(
                    publicKey,
                    this,
                    signature,
                    hash,
                    LedgerHandle.getHasher(ledgerHash)!!
                )
            }
        }
}