package org.knowledger.ledger.adapters.service

import com.github.michaelbull.result.binding
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.chain.handles.ChainHandle
import org.knowledger.ledger.chain.solver.StorageState
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.database.results.DataFailure
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.ok
import org.knowledger.ledger.storage.AdapterIds
import org.knowledger.ledger.storage.results.LoadFailure

internal class ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
    override val id: String get() = "ChainHandle"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "blockPool" to StorageType.LINK,
            "transactionPool" to StorageType.LINK,
            "difficultyTarget" to StorageType.DIFFICULTY,
            "lastRecalc" to StorageType.INTEGER,
            "currentBlockheight" to StorageType.LONG,
        )

    override fun update(element: ChainHandle, state: StorageState): Outcome<Unit, DataFailure> =
        store(element, state)

    override fun store(element: ChainHandle, state: StorageState): Outcome<Unit, DataFailure> =
        with(state) {
            pushNewLinked("chainId", element.chainId, AdapterIds.ChainId)
            pushNewLinked("blockPool", element.blockPool, AdapterIds.BlockPool)
            pushNewLinked("transactionPool", element.transactionPool, AdapterIds.TransactionPool)
            pushNewDifficulty("difficultyTarget", element.currentDifficulty)
            pushNewNative("lastRecalc", element.lastRecalculation)
            pushNewNative("currentBlockheight", element.currentBlockheight)
        }.ok()

    override fun load(
        ledgerHash: Hash, element: StorageElement, context: PersistenceContext,
    ): Outcome<ChainHandle, LoadFailure> =
        with(element) {
            val idElem = getLinked("chainId")

            val transactionPoolElem = getLinked("transactionPool")
            val blockPoolElem = getLinked("blockPool")

            binding {
                val chainId = context.chainIdStorageAdapter.load(ledgerHash, idElem, context).bind()
                val transactionPool =
                    context.transactionPoolStorageAdapter
                        .load(ledgerHash, transactionPoolElem, context)
                        .bind()
                val blockPool =
                    context.blockPoolStorageAdapter.load(ledgerHash, blockPoolElem, context).bind()

                val difficulty = getDifficultyProperty("difficultyTarget")

                val lastRecalc: Int = getStorageProperty("lastRecalc")

                val currentBlockheight: Long = getStorageProperty("currentBlockheight")

                val ch = ChainHandle(
                    context.ledgerInfo, context, chainId, transactionPool,
                    blockPool, difficulty, lastRecalc, currentBlockheight
                )
                ch
            }
        }
}
