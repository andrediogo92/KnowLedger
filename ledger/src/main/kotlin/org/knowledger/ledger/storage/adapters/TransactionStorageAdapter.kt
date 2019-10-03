package org.knowledger.ledger.storage.adapters


import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.transaction
import java.security.PublicKey

object TransactionStorageAdapter : LedgerStorageAdapter<Transaction> {
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
        toStore: Transaction,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            ).setLinked(
                "value",
                toStore.data.persist(session)
            ).setStorageBytes(
                "signature",
                session.newInstance(
                    toStore.signature
                )
            ).setHashProperty("hash", toStore.hash)

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<Transaction, LoadFailure> =
        tryOrLoadUnknownFailure {
            val physicalData = element.getLinked("value")

            physicalData.loadPhysicalData(
                ledgerHash
            ).mapSuccess { data ->
                val publicKey: PublicKey =
                    element
                        .getStorageProperty<ByteArray>("publicKey")
                        .toPublicKey()

                val signature =
                    element.getStorageBytes("signature").bytes

                val hash =
                    element.getHashProperty("hash")

                transaction(
                    publicKey, data,
                    signature, hash
                )
            }
        }
}