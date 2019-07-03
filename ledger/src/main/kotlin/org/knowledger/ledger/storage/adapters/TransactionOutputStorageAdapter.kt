package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.misc.byteEncodeToPublicKey
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.TransactionOutput

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
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionOutput, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKeyString: ByteArray =
                element.getStorageProperty("publicKey")
            val prevCoinbase =
                element.getHashProperty("prevCoinbase")
            val hashId =
                element.getHashProperty("hashId")
            val payout =
                element.getPayoutProperty("payout")
            val txSet = element.getHashSet("txSet")

            Outcome.Ok(
                TransactionOutput(
                    publicKeyString.byteEncodeToPublicKey(),
                    prevCoinbase,
                    hashId,
                    payout,
                    txSet,
                    LedgerHandle.getHasher(ledgerHash)!!
                )
            )
        }
}