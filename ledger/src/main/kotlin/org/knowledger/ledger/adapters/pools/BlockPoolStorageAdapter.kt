package org.knowledger.ledger.adapters.pools

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.combine
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.adapters.LedgerStorageAdapter
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
import org.knowledger.ledger.storage.MutableBlockPool
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class BlockPoolStorageAdapter : LedgerStorageAdapter<MutableBlockPool> {
    override val id: String
        get() = "BlockPool"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "blocks" to StorageType.LIST
        )

    override fun store(
        element: MutableBlockPool, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewLinked("chainId", element.chainId, AdapterIds.ChainId)
                pushNewLinkedList("blocks", element.mutableBlocks, AdapterIds.Block)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext
    ): Outcome<MutableBlockPool, LoadFailure> =
        tryOrLoadUnknownFailure {
            val blockElem = element.getMutableElementList("blocks")
            val chainElem = element.getLinked("chainId")
            val blockAdapter = context.blockStorageAdapter
            binding<MutableBlockPool, LoadFailure> {
                val blocks = blockElem.map { block ->
                    blockAdapter.load(ledgerHash, block, context)
                }.combine().bind()
                val chainId = context.chainIdStorageAdapter.load(
                    ledgerHash, chainElem, context
                ).bind()
                context.blockPoolFactory.create(
                    chainId, blocks.toMutableSortedListFromPreSorted()
                )
            }
        }

}