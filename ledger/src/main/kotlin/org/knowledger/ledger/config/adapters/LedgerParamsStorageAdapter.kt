package org.knowledger.ledger.config.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

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