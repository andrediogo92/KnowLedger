package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.config.adapters.LedgerParamsStorageAdapter
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.results.tryOrLedgerQueryFailure
import pt.um.masb.ledger.service.ChainHandle
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.service.results.LoadResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper

class ChainHandleStorageAdapter : ServiceStorageAdapter<ChainHandle> {
    val ledgerParamsStorageAdapter = LedgerParamsStorageAdapter()

    override val id: String
        get() = "ChainHandle"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "clazz" to StorageType.STRING,
            "hashId" to StorageType.BYTES,
            "difficultyTarget" to StorageType.BYTES,
            "lastRecalc" to StorageType.INTEGER,
            "currentBlockheight" to StorageType.LONG,
            "params" to StorageType.LINK
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
            setLinked(
                "params", ledgerParamsStorageAdapter,
                toStore.params, session
            )
        }

    override fun load(
        persistenceWrapper: PersistenceWrapper,
        hash: Hash, element: StorageElement
    ): LedgerResult<ChainHandle> =
        tryOrLedgerQueryFailure {
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

            val ledgerParams: LoadResult<LedgerParams> =
                ledgerParamsStorageAdapter.load(
                    hash,
                    element.getLinked("params")
                )

            ledgerParams.intoLedger {
                ChainHandle(
                    persistenceWrapper,
                    this,
                    clazz,
                    hash,
                    difficulty,
                    lastRecalc,
                    currentBlockheight
                )
            }
        }
}