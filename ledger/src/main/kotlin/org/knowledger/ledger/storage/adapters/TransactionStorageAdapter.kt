package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.LedgerContainer
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl

object TransactionStorageAdapter : LedgerStorageAdapter<HashedTransaction> {
    override val id: String
        get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "value" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hash" to StorageType.HASH
        )

    override fun store(
        toStore: HashedTransaction,
        session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            ).setLinked(
                "value", PhysicalDataStorageAdapter,
                toStore.data, session
            ).setStorageBytes(
                "signature",
                session.newInstance(
                    toStore.signature
                )
            ).setHashProperty("hash", toStore.hash)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<HashedTransaction, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKeyString: ByteArray =
                element.getStorageProperty("publicKey")

            PhysicalDataStorageAdapter.load(
                ledgerHash,
                element.getLinked("value")
            ).mapSuccess { data ->
                val signature =
                    element.getStorageBytes("signature").bytes

                val hash =
                    element.getHashProperty("hash")

                val container: LedgerContainer =
                    LedgerHandle.getContainer(ledgerHash)!!

                HashedTransactionImpl(
                    publicKeyString.toPublicKey(),
                    data, signature, hash,
                    container.hasher, container.cbor
                )
            }
        }
}