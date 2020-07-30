package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.combine
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewLinkedList
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class CoinbaseStorageAdapter : LedgerStorageAdapter<MutableCoinbase> {
    override val id: String
        get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "coinbaseHeader" to StorageType.LINK,
            "merkleTree" to StorageType.LINK,
            "witnesses" to StorageType.LIST
        )

    override fun store(
        element: MutableCoinbase, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewLinked("coinbaseHeader", element.coinbaseHeader, AdapterIds.CoinbaseHeader)
                pushNewLinked("merkleTree", element.merkleTree, AdapterIds.MerkleTree)
                pushNewLinkedList("witnesses", element.mutableWitnesses, AdapterIds.Witness)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<MutableCoinbase, LoadFailure> =
        element.cachedLoad {
            tryOrLoadUnknownFailure {
                val merkleTree = element.getLinked("merkleTree")
                val header = element.getLinked("coinbaseHeader")
                val witnesses = element.getElementList("witnesses")
                binding<MutableCoinbase, LoadFailure> {
                    val coinbaseHeader = context.coinbaseHeaderStorageAdapter.load(
                        ledgerHash, header, context
                    ).bind()
                    val merkleTree = context.merkleTreeStorageAdapter.load(
                        ledgerHash, merkleTree, context
                    ).bind()
                    val witnesses = witnesses.map {
                        val adapter = context.witnessStorageAdapter
                        adapter.load(ledgerHash, element, context)
                    }.combine().bind()
                    context.coinbaseFactory.create(
                        merkleTree = merkleTree, coinbaseHeader = coinbaseHeader,
                        witnesses = witnesses.toMutableSortedListFromPreSorted()
                    )
                }
            }
        }

}