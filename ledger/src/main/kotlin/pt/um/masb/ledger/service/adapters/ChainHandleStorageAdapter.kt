package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.results.LedgerFailure
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

object ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
    override val id: String
        get() = "ChainHandle"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "clazz" to StorageType.STRING,
            "hashId" to StorageType.HASH,
            "difficultyTarget" to StorageType.DIFFICULTY,
            "lastRecalc" to StorageType.INTEGER,
            "currentBlockheight" to StorageType.LONG
        )

    override fun store(
        toStore: ChainHandle,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty("clazz", toStore.clazz)
            setHashProperty("hashId", toStore.ledgerHash)
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
        persistenceWrapper: PersistenceWrapper,
        element: StorageElement
    ): Outcome<ChainHandle, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val clazz: String =
                element.getStorageProperty("clazz")

            val hash =
                element.getHashProperty("hashId")

            val difficulty =
                element.getDifficultyProperty("difficultyTarget")

            val lastRecalc: Long =
                element.getStorageProperty("lastRecalc")

            val currentBlockheight: Long =
                element.getStorageProperty("currentBlockheight")

            Outcome.Ok<ChainHandle, LedgerFailure>(
                ChainHandle(
                    clazz,
                    hash,
                    difficulty,
                    lastRecalc,
                    currentBlockheight
                )
            )

        }
}