package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transactionOutput
import java.security.PublicKey

object TransactionOutputStorageAdapter : LedgerStorageAdapter<TransactionOutput> {
    override val id: String
        get() = "TransactionOutput"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "prevCoinbase" to StorageType.HASH,
            "hash" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "txSet" to StorageType.SET
        )

    override fun store(
        toStore: TransactionOutput,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            ).setHashProperty(
                "prevCoinbase", toStore.previousCoinbase
            ).setHashProperty("hash", toStore.hash)
            .setPayoutProperty("payout", toStore.payout)
            .setHashSet("txSet", toStore.transactionHashes)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<TransactionOutput, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKey: PublicKey =
                element
                    .getStorageProperty<ByteArray>("publicKey")
                    .toPublicKey()
            val prevCoinbase =
                element.getHashProperty("prevCoinbase")
            val hash =
                element.getHashProperty("hash")
            val payout =
                element.getPayoutProperty("payout")
            val txSet = element.getMutableHashSet("txSet")
            val container = LedgerHandle.getContainer(ledgerHash)

            container?.let {
                Outcome.Ok(
                    transactionOutput(
                        publicKey, prevCoinbase, payout,
                        txSet, hash, it.hasher, it.encoder
                    )
                )
            } ?: Outcome.Error(
                LoadFailure.NoMatchingContainer(ledgerHash)
            )

        }
}