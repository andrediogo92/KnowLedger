package org.knowledger.ledger.adapters.pools

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.combine
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.MutableBlockPool
import org.knowledger.ledger.storage.results.LoadFailure

internal class BlockPoolStorageAdapter : LedgerStorageAdapter<MutableBlockPool> {
    override val id: String get() = "BlockPool"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "blocks" to StorageType.LIST,
        )

    override fun store(
        element: MutableBlockPool, state: StorageState,
    ): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewLinked("chainId", element.chainId, AdapterIds.ChainId)
            pushNewLinkedList("blocks", element.mutableBlocks, AdapterIds.Block)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<MutableBlockPool, LoadFailure> =
        with(element) {
            val blockElem = getMutableElementList("blocks")
            val chainElem = getLinked("chainId")
            val blockAdapter = context.blockStorageAdapter
            binding {
                val blocks = blockElem.map { block ->
                    blockAdapter.load(ledgerHash, block, context)
                }.combine().bind()
                val chainId = context.chainIdStorageAdapter.load(
                    ledgerHash, chainElem, context,
                ).bind()
                context.blockPoolFactory.create(chainId, blocks.toMutableSortedListFromPreSorted())
            }
        }

}