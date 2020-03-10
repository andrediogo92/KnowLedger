package org.knowledger.ledger.storage.transaction

import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import java.security.PublicKey

internal class SUTransactionStorageAdapter(
    private val physicalDataStorageAdapter: PhysicalDataStorageAdapter
) : LedgerStorageAdapter<HashedTransactionImpl> {
    override val id: String
        get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "data" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hash" to StorageType.HASH,
            "index" to StorageType.INTEGER
        )

    override fun store(
        toStore: HashedTransactionImpl, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "publicKey", toStore.publicKey.encoded
            ).setLinked(
                "data",
                physicalDataStorageAdapter.persist(
                    toStore.data, session
                )
            ).setStorageBytes(
                "signature",
                session.newInstance(
                    toStore.signature.bytes
                )
            ).setHashProperty("hash", toStore.hash)
            .setStorageProperty("index", toStore.index)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<HashedTransactionImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val physicalData = element.getLinked("data")

            physicalDataStorageAdapter.load(
                ledgerHash,
                physicalData
            ).mapSuccess { data ->
                val publicKey: PublicKey = EncodedPublicKey(
                    element.getStorageProperty("publicKey")
                ).toPublicKey()
                val signature =
                    element.getStorageBytes("signature").bytes

                val hash =
                    element.getHashProperty("hash")

                val index =
                    element.getStorageProperty<Int>("index")

                HashedTransactionImpl(
                    publicKey = publicKey, data = data,
                    signature = signature, hash = hash,
                    index = index
                )
            }
        }

}