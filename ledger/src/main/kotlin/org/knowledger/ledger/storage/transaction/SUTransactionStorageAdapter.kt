package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter
import org.knowledger.ledger.storage.adapters.loadPhysicalData
import org.knowledger.ledger.storage.adapters.persist
import java.security.PublicKey

internal object SUTransactionStorageAdapter : LedgerStorageAdapter<HashedTransactionImpl> {
    override val id: String
        get() = TransactionStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = TransactionStorageAdapter.properties

    override fun store(
        toStore: HashedTransactionImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(TransactionStorageAdapter.id)
            .setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            ).setLinked(
                "value",
                toStore.data.persist(session)
            ).setStorageBytes(
                "signature",
                session.newInstance(
                    toStore.signature.encoded
                )
            ).setHashProperty("hash", toStore.hash)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedTransactionImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val physicalData = element.getLinked("value")

            physicalData.loadPhysicalData(
                ledgerHash
            ).mapSuccess { data ->
                val publicKey: PublicKey = EncodedPublicKey(
                    element.getStorageProperty("publicKey")
                ).toPublicKey()
                val signature =
                    element.getStorageBytes("signature").bytes

                val hash =
                    element.getHashProperty("hash")

                HashedTransactionImpl(
                    publicKey, data,
                    signature, hash
                )
            }
        }

}