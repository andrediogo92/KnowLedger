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
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.results.LoadFailure

internal class BlockStorageAdapter : LedgerStorageAdapter<MutableBlock> {
    override val id: String get() = "Block"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "blockHeader" to StorageType.LINK,
            "coinbase" to StorageType.LINK,
            "merkleTree" to StorageType.LINK,
            "transactions" to StorageType.LIST,
        )

    override fun store(element: MutableBlock, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewLinked("blockHeader", element.blockHeader, AdapterIds.BlockHeader)
            pushNewLinked("coinbase", element.coinbase, AdapterIds.Coinbase)
            pushNewLinked("merkleTree", element.merkleTree, AdapterIds.MerkleTree)
            pushNewLinkedList("transactions", element.mutableTransactions, AdapterIds.Transaction)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableBlock, LoadFailure> =
        element.cachedLoad {
            val headerElem = getLinked("blockHeader")
            val coinbaseElem = getLinked("coinbase")
            val merkleTreeElem = getLinked("merkleTree")
            val transactionsElem = getElementList("transactions")
            binding {
                val blockHeader = context.blockHeaderStorageAdapter.load(
                    ledgerHash, headerElem, context,
                ).bind()
                val coinbase = context.coinbaseStorageAdapter.load(
                    ledgerHash, coinbaseElem, context,
                ).bind()
                val merkleTree = context.merkleTreeStorageAdapter.load(
                    ledgerHash, merkleTreeElem, context,
                ).bind()
                val transactionAdapter = context.transactionStorageAdapter
                val transactions = transactionsElem.map {
                    transactionAdapter.load(ledgerHash, it, context)
                }.combine().bind()
                context.blockFactory.create(
                    blockHeader, coinbase, merkleTree,
                    transactions.toMutableSortedListFromPreSorted(),
                )
            }
        }

}