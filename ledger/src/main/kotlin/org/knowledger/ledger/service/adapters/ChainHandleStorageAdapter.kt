package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.adapters.ChainIdStorageAdapter
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.results.zip
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.handles.ChainHandle
import org.knowledger.ledger.service.results.LedgerFailure

internal class ChainHandleStorageAdapter(
    private val adapterManager: AdapterManager,
    private val container: LedgerInfo,
    private val transactionPoolStorageAdapter: TransactionPoolStorageAdapter
) : ServiceStorageAdapter<ChainHandle> {
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
            .setLinked(
                "id", ChainIdStorageAdapter.persist(
                    toStore.id, session
                )
            )
            .setLinked(
                "transactionPool",
                transactionPoolStorageAdapter.persist(
                    toStore.transactionPool, session
                )
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
                ChainIdStorageAdapter.load(
                    ledgerHash, id
                ),
                transactionPoolStorageAdapter.load(
                    ledgerHash,
                    transactionPool
                )
            ) { id, transactionPool ->
                val difficulty =
                    element.getDifficultyProperty("difficultyTarget")

                val lastRecalc: Long =
                    element.getStorageProperty("lastRecalc")

                val currentBlockheight: Long =
                    element.getStorageProperty("currentBlockheight")


                ChainHandle(
                    container,
                    id,
                    transactionPool,
                    difficulty,
                    lastRecalc,
                    currentBlockheight
                )
            }
        }
}