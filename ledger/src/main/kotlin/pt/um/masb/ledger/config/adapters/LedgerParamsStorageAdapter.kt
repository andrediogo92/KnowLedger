package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.mapSuccess
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.service.adapters.ServiceStorageAdapter
import pt.um.masb.ledger.service.results.LedgerFailure

object LedgerParamsStorageAdapter : ServiceStorageAdapter<LedgerParams> {
    override val id: String
        get() = "LedgerParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "crypter" to StorageType.BYTES,
            "recalcTime" to StorageType.LONG,
            "recalcTrigger" to StorageType.LONG,
            "blockParams" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerParams, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setHashProperty("crypter", toStore.crypter)
            setStorageProperty(
                "recalcTime", toStore.recalcTime
            )
            setStorageProperty(
                "recalcTrigger", toStore.recalcTrigger
            )
            setLinked(
                "blockParams", BlockParamsStorageAdapter,
                toStore.blockParams, session
            )
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<LedgerParams, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            BlockParamsStorageAdapter.load(
                ledgerHash,
                element.getLinked("blockParams")
            ).mapSuccess {
                LedgerParams(
                    element.getHashProperty("crypter"),
                    element.getStorageProperty("recalcTime"),
                    element.getStorageProperty("recalcTrigger"),
                    it
                )
            }

        }
}