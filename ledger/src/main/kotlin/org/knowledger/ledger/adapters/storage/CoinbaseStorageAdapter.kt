package org.knowledger.ledger.adapters.storage

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.combine
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.adapters.cachedLoad
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.results.LoadFailure

internal class CoinbaseStorageAdapter : LedgerStorageAdapter<MutableCoinbase> {
    override val id: String get() = "Coinbase"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "coinbaseHeader" to StorageType.LINK,
            "merkleTree" to StorageType.LINK,
            "witnesses" to StorageType.LIST,
        )

    override fun store(
        element: MutableCoinbase, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewLinked("coinbaseHeader", element.coinbaseHeader, AdapterIds.CoinbaseHeader)
            pushNewLinked("merkleTree", element.merkleTree, AdapterIds.MerkleTree)
            pushNewLinkedList("witnesses", element.mutableWitnesses, AdapterIds.Witness)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableCoinbase, LoadFailure> =
        element.cachedLoad {
            val merkleTreeElem = getLinked("merkleTree")
            val headerElem = getLinked("coinbaseHeader")
            val witnessesElem = getElementList("witnesses")
            binding {
                val coinbaseHeader = context.coinbaseHeaderStorageAdapter.load(
                    ledgerHash, headerElem, context,
                ).bind()
                val merkleTree = context.merkleTreeStorageAdapter.load(
                    ledgerHash, merkleTreeElem, context,
                ).bind()
                val witnessAdapter = context.witnessStorageAdapter
                val witnesses = witnessesElem.map {
                    witnessAdapter.load(ledgerHash, element, context)
                }.combine().bind()
                context.coinbaseFactory.create(
                    coinbaseHeader, merkleTree, witnesses.toMutableSortedListFromPreSorted(),
                )
            }

        }

}