package org.knowledger.ledger.adapters.service

import com.github.michaelbull.result.binding
import com.github.michaelbull.result.mapError
import org.knowledger.ledger.core.tryOrDataUnknownFailure
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.solver.StorageSolver
import org.knowledger.ledger.service.solver.pushNewDifficulty
import org.knowledger.ledger.service.solver.pushNewLinked
import org.knowledger.ledger.service.solver.pushNewNative
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.results.LedgerFailure
import org.knowledger.ledger.storage.results.intoLedger
import org.knowledger.ledger.storage.results.tryOrLedgerUnknownFailure

internal class ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
    override val id: String
        get() = "ChainHandle"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "transactionPool" to StorageType.LINK,
            "difficultyTarget" to StorageType.DIFFICULTY,
            "lastRecalc" to StorageType.INTEGER,
            "currentBlockheight" to StorageType.LONG
        )

    override fun update(
        element: ChainHandle, solver: StorageSolver
    ): Outcome<Unit, DataFailure> = store(element, solver)

    override fun store(
        element: ChainHandle, solver: StorageSolver
    ): Outcome<Unit, DataFailure> =
        tryOrDataUnknownFailure {
            with(solver) {
                pushNewLinked("chainId", element.chainId, AdapterIds.ChainId)
                pushNewLinked("transactionPool", element.transactionPool, AdapterIds.TransactionPool)
                pushNewDifficulty("difficultyTarget", element.currentDifficulty)
                pushNewNative("lastRecalc", element.lastRecalculation)
                pushNewNative("currentBlockheight", element.currentBlockheight)
            }.ok()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement,
        context: PersistenceContext
    ): Outcome<ChainHandle, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val idElem =
                element.getLinked("chainId")

            val transactionPoolElem =
                element.getLinked("transactionPool")

            binding<ChainHandle, LedgerFailure> {
                val chainId = context.chainIdStorageAdapter.load(
                    ledgerHash, idElem, context
                ).mapError { it.intoLedger() }.bind()
                val transactionPool = context.transactionPoolStorageAdapter.load(
                    ledgerHash, transactionPoolElem, context
                ).mapError { it.intoLedger() }.bind()

                val difficulty =
                    element.getDifficultyProperty("difficultyTarget")

                val lastRecalc: Int =
                    element.getStorageProperty("lastRecalc")

                val currentBlockheight: Long =
                    element.getStorageProperty("currentBlockheight")


                ChainHandle(
                    context.ledgerInfo, context, chainId, transactionPool,
                    difficulty, lastRecalc, currentBlockheight
                )
            }
        }
}