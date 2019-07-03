package org.knowledger.ledger.storage.adapters


import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.misc.byteEncodeToPublicKey
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapFailure
import org.knowledger.common.results.zip
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.data.adapters.PhysicalDataStorageAdapter
import org.knowledger.ledger.results.intoLoad
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Transaction

object TransactionStorageAdapter : LedgerStorageAdapter<Transaction> {
    override val id: String
        get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "chainId" to StorageType.LINK,
            "value" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hashId" to StorageType.HASH
        )

    override fun store(
        toStore: Transaction,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty("publicKey", toStore.publicKey.encoded)
            setLinked(
                "chainId", ChainIdStorageAdapter,
                toStore.chainId, session
            )
            setLinked(
                "value", PhysicalDataStorageAdapter,
                toStore.data, session
            )
            setStorageBytes(
                "signature",
                session.newInstance(
                    toStore.signature
                )
            )
            setHashProperty("hashId", toStore.hashId)
        }

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<Transaction, LoadFailure> =
        tryOrLoadUnknownFailure {
            val publicKey = byteEncodeToPublicKey(
                element.getStorageProperty("publicKey")
            )

            zip(
                ChainIdStorageAdapter.load(
                    ledgerHash, element.getLinked("chainId")
                ).mapFailure {
                    it.intoLoad()
                },
                PhysicalDataStorageAdapter.load(
                    ledgerHash,
                    element.getLinked("value")
                )
            ) { chainId, data ->
                val signature =
                    element.getStorageBytes("signature").bytes

                val hash =
                    element.getHashProperty("hashId")

                Transaction(
                    chainId,
                    publicKey,
                    data,
                    signature,
                    hash,
                    LedgerHandle.getHasher(ledgerHash)!!
                )
            }
        }
}