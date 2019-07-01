package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.ledger.config.adapters.ChainIdStorageAdapter
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.service.handles.ChainHandle
import pt.um.masb.ledger.service.results.LedgerFailure

object ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
    override val id: String
        get() = "ChainHandle"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "id" to StorageType.LINK,
            "difficultyTarget" to StorageType.DIFFICULTY,
            "lastRecalc" to StorageType.INTEGER,
            "currentBlockheight" to StorageType.LONG
        )

    override fun store(
        toStore: ChainHandle,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setLinked(
                "id", ChainIdStorageAdapter,
                toStore.id, session
            )
            setDifficultyProperty(
                "difficultyTarget",
                toStore.currentDifficulty,
                session
            )
            setStorageProperty(
                "lastRecalc",
                toStore.lastRecalculation
            )
            setStorageProperty(
                "currentBlockheight",
                toStore.currentBlockheight
            )
        }

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

            ChainIdStorageAdapter
                .load(ledgerHash, element.getLinked("tag"))
                .mapSuccess {
                    ChainHandle(
                        it,
                        difficulty,
                        lastRecalc,
                        currentBlockheight
                    )
                }


        }
}