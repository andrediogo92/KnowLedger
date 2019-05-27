package pt.um.masb.ledger.storage.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.misc.byteEncodeToPublicKey
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.LedgerHandle
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.TransactionOutput

object TransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutput> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "prevCoinbase" to StorageType.HASH,
            "hashId" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "txSet" to StorageType.SET
        )

    override fun store(
        toStore: TransactionOutput,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            this
                .setStorageProperty(
                    "publicKey", toStore.publicKey.encoded
                ).setHashProperty(
                    "prevCoinbase", toStore.prevCoinbase
                ).setHashProperty("hashId", toStore.hashId)
                .setPayoutProperty("payout", toStore.payout)
                .setHashSet("txSet", toStore.tx)
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionOutput, LoadFailure> =
        tryOrLoadUnknownFailure {
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

            Outcome.Ok<TransactionOutput, LoadFailure>(
                TransactionOutput(
                    publicKey,
                    prevCoinbase,
                    hashId,
                    payout,
                    txSet,
                    LedgerHandle.getHasher(ledgerHash)!!
                )
            )
        }
}