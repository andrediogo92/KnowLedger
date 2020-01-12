package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import java.security.PublicKey

internal class SUTransactionOutputStorageAdapter(
    private val container: LedgerInfo
) : LedgerStorageAdapter<HashedTransactionOutputImpl> {
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
        toStore: HashedTransactionOutputImpl, session: ManagedSession
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
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedTransactionOutputImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKey: PublicKey = EncodedPublicKey(
                element.getStorageProperty("publicKey")
            ).toPublicKey()
            val prevCoinbase =
                element.getHashProperty("prevCoinbase")
            val hash =
                element.getHashProperty("hash")
            val payout =
                element.getPayoutProperty("payout")
            val txSet = element.getMutableHashSet("txSet")

            Outcome.Ok(
                HashedTransactionOutputImpl(
                    publicKey, prevCoinbase, payout,
                    txSet, hash, container.hasher, container.encoder
                )
            )
        }
}