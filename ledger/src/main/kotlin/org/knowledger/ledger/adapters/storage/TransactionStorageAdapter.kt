package org.knowledger.ledger.adapters.storage


import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.results.LoadFailure

internal class TransactionStorageAdapter : LedgerStorageAdapter<MutableTransaction> {
    override val id: String get() = "Transaction"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "data" to StorageType.LINK,
            "signature" to StorageType.LINK,
            "hash" to StorageType.HASH,
            "index" to StorageType.INTEGER,
        )

    override fun store(
        element: MutableTransaction, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("publicKey", element.publicKey.bytes)
            pushNewLinked("data", element.data, AdapterIds.PhysicalData)
            pushNewBytes("signature", element.signature.bytes)
            pushNewHash("hash", element.hash)
            pushNewNative("index", element.index)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableTransaction, LoadFailure> =
        element.cachedLoad {
            val physicalData = getLinked("data")

            context.physicalDataStorageAdapter
                .load(ledgerHash, physicalData, context)
                .map { data ->
                    val publicKey = EncodedPublicKey(getStorageProperty("publicKey"))
                    val signature = EncodedSignature(getStorageBytes("signature").bytes)

                    val hash = getHashProperty("hash")

                    val index: Int = getStorageProperty("index")

                    val mht = context.transactionFactory.create(
                        publicKey, data, signature, context.ledgerInfo.hashers,
                        context.ledgerInfo.encoder,
                    )
                    assert(mht.hash == hash)
                    mht.markIndex(index)
                    mht
                }
        }

}