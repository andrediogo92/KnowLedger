package org.knowledger.ledger.adapters.config

import org.knowledger.ledger.adapters.LedgerStorageAdapter
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.results.LoadFailure

internal class BlockParamsStorageAdapter : LedgerStorageAdapter<BlockParams> {
    override val id: String get() = "BlockParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "blockMemorySize" to StorageType.INTEGER,
            "blockLength" to StorageType.INTEGER,
        )

    override fun store(element: BlockParams, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewNative("blockMemorySize", element.blockMemorySize)
            pushNewNative("blockLength", element.blockLength)
        }.ok()


    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<BlockParams, LoadFailure> =
        with(element) {
            val blockMemorySize: Int = getStorageProperty("blockMemorySize")
            val blockLength: Int = getStorageProperty("blockLength")
            context.blockParamsFactory.create(blockMemorySize, blockLength).ok()
        }

}