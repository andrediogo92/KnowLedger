package org.knowledger.ledger.storage.transaction.output

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionOutputStorageAdapter
import java.security.PublicKey

internal object SUTransactionOutputStorageAdapter : LedgerStorageAdapter<HashedTransactionOutputImpl> {
    override val id: String
        get() = TransactionOutputStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionOutputStorageAdapter.properties

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
                    HashedTransactionOutputImpl(
                        publicKey, prevCoinbase, payout,
                        txSet, hash, it.hasher, it.encoder
                    )
                )
            } ?: Outcome.Error(
                LoadFailure.NoMatchingContainer(ledgerHash)
            )

        }
}