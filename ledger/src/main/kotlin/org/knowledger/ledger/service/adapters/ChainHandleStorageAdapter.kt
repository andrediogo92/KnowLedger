package org.knowledger.ledger.service.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.zip
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.results.LedgerFailure

object ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
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
        session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "id", ChainIdStorageAdapter,
                toStore.id, session
            ).setLinked(
                "transactionPool", TransactionPoolStorageAdapter,
                toStore.transactionPool, session
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

    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainHandle, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val difficulty =
                element.getDifficultyProperty("difficultyTarget")

            val lastRecalc: Long =
                element.getStorageProperty("lastRecalc")

            val currentBlockheight: Long =
                element.getStorageProperty("currentBlockheight")

            zip(
                ChainIdStorageAdapter
                    .load(ledgerHash, element.getLinked("tag")),
                TransactionPoolStorageAdapter.load(
                    ledgerHash, element.getLinked("transactionPool")
                )
            ) { id, transactionPool ->
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