package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.combine
import com.github.michaelbull.result.map
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewHash
import org.knowledger.ledger.service.solver.pushNewLinkedList
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.service.solver.pushNewPayout
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class WitnessStorageAdapter : LedgerStorageAdapter<MutableWitness> {
    override val id: String
        get() = "Witness"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "previousWitnessIndex" to StorageType.INTEGER,
            "previousCoinbase" to StorageType.HASH,
            "index" to StorageType.INTEGER,
            "hash" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "transactionOutputs" to StorageType.LIST
        )

    override fun store(
        element: MutableWitness, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("publicKey", element.publicKey.bytes)
                pushNewNative("previousWitnessIndex", element.previousWitnessIndex)
                pushNewHash("previousCoinbase", element.previousCoinbase)
                pushNewHash("hash", element.hash)
                pushNewNative("index", element.index)
                pushNewPayout("payout", element.payout)
                pushNewLinkedList(
                    "transactionOutputs", element.mutableTransactionOutputs,
                    AdapterIds.TransactionOutput
                )
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<MutableWitness, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                element.getElementList("transactionOutputs").map {
                    val adapter = context.transactionOutputStorageAdapter
                    adapter.load(ledgerHash, it, context)
                }.combine().map {
                    val publicKey = EncodedPublicKey(element.getStorageProperty("publicKey"))
                    val previousWitnessIndex: Int = element.getStorageProperty("previousWitnessIndex")
                    val previousCoinbase = element.getHashProperty("previousCoinbase")
                    val hash = element.getHashProperty("hash")
                    val index: Int = element.getStorageProperty("index")
                    val payout = element.getPayoutProperty("payout")

                    val hwi = context.witnessFactory.create(
                        publicKey = publicKey, previousWitnessIndex = previousWitnessIndex,
                        previousCoinbase = previousCoinbase, payout = payout,
                        transactionOutputs = it.toMutableSortedListFromPreSorted(),
                        hasher = context.ledgerInfo.hashers, encoder = context.ledgerInfo.encoder
                    )
                    assert(hash == hwi.hash)
                    hwi.markIndex(index)
                    hwi
                }

            }
        }
}