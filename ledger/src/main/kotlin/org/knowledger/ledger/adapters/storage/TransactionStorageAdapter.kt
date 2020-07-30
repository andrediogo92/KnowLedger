package org.knowledger.ledger.adapters.storage


import com.github.michaelbull.result.map
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewBytes
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure
import java.security.PublicKey

internal class TransactionStorageAdapter : LedgerStorageAdapter<MutableTransaction> {
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
        element: MutableTransaction, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("publicKey", element.publicKey.encoded)
                pushNewLinked("data", element.data, AdapterIds.PhysicalData)
                pushNewBytes("signature", element.signature.bytes)
                pushNewHash("hash", element.hash)
                pushNewNative("index", element.index)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext
    ): Outcome<MutableTransaction, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val physicalData = element.getLinked("data")

                context.physicalDataStorageAdapter.load(
                    ledgerHash, physicalData, context
                ).map { data ->
                    val publicKey: PublicKey = EncodedPublicKey(
                        element.getStorageProperty("publicKey")
                    ).toPublicKey()
                    val signature = EncodedSignature(
                        element.getStorageBytes("signature").bytes
                    )

                    val hash = element.getHashProperty("hash")

                    val index = element.getStorageProperty<Int>("index")

                    val mht = context.transactionFactory.create(
                        publicKey = publicKey, data = data,
                        signature = signature, hasher = context.ledgerInfo.hashers,
                        encoder = context.ledgerInfo.encoder
                    )
                    assert(mht.hash == hash)
                    mht.markIndex(index)
                    mht
                }
            }
        }

}