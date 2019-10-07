package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.config.adapters.loadChainId
import org.knowledger.ledger.config.adapters.persist
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.results.LedgerFailure

internal object ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
    override val id: String
        get() = "ChainHandle"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.LINK,
            "transactionPool" to StorageType.LINK,
            "difficultyTarget" to StorageType.DIFFICULTY,
            "lastRecalc" to StorageType.INTEGER,
            "currentBlockheight" to StorageType.LONG
        )

    override fun store(
        toStore: ChainHandle,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked("id", toStore.id.persist(session))
            .setLinked(
                "transactionPool",
                toStore.transactionPool.persist(session)
            ).setDifficultyProperty(
                "difficultyTarget",
                toStore.currentDifficulty,
                session
            ).setStorageProperty(
                "lastRecalc",
                toStore.lastRecalculation
            ).setStorageProperty(
                "currentBlockheight",
                toStore.currentBlockheight
            )

    @Suppress("NAME_SHADOWING")
    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainHandle, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val id =
                element.getLinked("id")

            val transactionPool =
                element.getLinked("transactionPool")

            zip(
                id.loadChainId(ledgerHash),
                transactionPool.loadTransactionPool(ledgerHash)
            ) { id, transactionPool ->
                val difficulty =
                    element.getDifficultyProperty("difficultyTarget")

                val lastRecalc: Long =
                    element.getStorageProperty("lastRecalc")

                val currentBlockheight: Long =
                    element.getStorageProperty("currentBlockheight")


                ChainHandle(
                    id,
                    transactionPool,
                    difficulty,
                    lastRecalc,
                    currentBlockheight
                )
            }
        }
}