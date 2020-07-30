package org.knowledger.ledger.adapters.config

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
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.config.block.BlockParams
import org.knowledger.ledger.storage.results.LoadFailure
import org.knowledger.ledger.storage.results.tryOrLoadUnknownFailure

internal class BlockParamsStorageAdapter : LedgerStorageAdapter<BlockParams> {
    override val id: String
        get() = "BlockParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "blockMemorySize" to StorageType.INTEGER,
            "blockLength" to StorageType.INTEGER
        )

    override fun store(
        element: BlockParams, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewNative("blockMemorySize", element.blockMemorySize)
                pushNewNative("blockLength", element.blockLength)
            }.ok()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<BlockParams, LoadFailure> =
        tryOrLoadUnknownFailure {
            val blockMemorySize: Int = element.getStorageProperty("blockMemorySize")
            val blockLength: Int = element.getStorageProperty("blockLength")
            context.blockParamsFactory.create(blockMemorySize, blockLength).ok()
        }

}