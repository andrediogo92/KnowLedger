package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.combine
import com.github.michaelbull.result.map
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableWitness
import org.knowledger.ledger.storage.results.LoadFailure

internal class WitnessStorageAdapter : LedgerStorageAdapter<MutableWitness> {
    override val id: String get() = "Witness"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "publicKey" to StorageType.BYTES,
            "previousWitnessIndex" to StorageType.INTEGER,
            "previousCoinbase" to StorageType.HASH,
            "index" to StorageType.INTEGER,
            "hash" to StorageType.HASH,
            "payout" to StorageType.PAYOUT,
            "transactionOutputs" to StorageType.LIST,
        )

    override fun store(element: MutableWitness, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("publicKey", element.publicKey.bytes)
            pushNewNative("previousWitnessIndex", element.previousWitnessIndex)
            pushNewHash("previousCoinbase", element.previousCoinbase)
            pushNewHash("hash", element.hash)
            pushNewNative("index", element.index)
            pushNewPayout("payout", element.payout)
            pushNewLinkedList(
                "transactionOutputs", element.mutableTransactionOutputs,
                AdapterIds.TransactionOutput,
            )
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableWitness, LoadFailure> =
        element.cachedLoad {
            getElementList("transactionOutputs").map {
                val adapter = context.transactionOutputStorageAdapter
                adapter.load(ledgerHash, it, context)
            }.combine().map { txos ->
                val publicKey = EncodedPublicKey(getStorageProperty("publicKey"))
                val previousWitnessIndex: Int = getStorageProperty("previousWitnessIndex")
                val previousCoinbase = getHashProperty("previousCoinbase")
                val hash = getHashProperty("hash")
                val index: Int = getStorageProperty("index")
                val payout = getPayoutProperty("payout")

                val hwi = context.witnessFactory.create(
                    publicKey, previousWitnessIndex, previousCoinbase,
                    payout, txos.toMutableSortedListFromPreSorted(),
                    context.ledgerInfo.hashers, context.ledgerInfo.encoder,
                )
                assert(hash == hwi.hash)
                hwi.markIndex(index)
                hwi
            }

        }
}