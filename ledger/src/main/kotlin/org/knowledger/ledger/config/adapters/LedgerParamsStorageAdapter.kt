package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.LedgerParams
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.mapSuccess
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object LedgerParamsStorageAdapter : ServiceStorageAdapter<LedgerParams> {
    override val id: String
        get() = "LedgerParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "crypter" to StorageType.HASH,
            "recalculationTime" to StorageType.LONG,
            "recalculationTrigger" to StorageType.INTEGER,
            "blockParams" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerParams, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashProperty("crypter", toStore.hasher)
            .setStorageProperty(
                "recalculationTime", toStore.recalculationTime
            ).setStorageProperty(
                "recalculationTrigger", toStore.recalculationTrigger
            ).setLinked(
                "blockParams",
                BlockParamsStorageAdapter.persist(
                    toStore.blockParams, session
                )
            )


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<LedgerParams, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val blockParams = element.getLinked("blockParams")

            BlockParamsStorageAdapter.load(
                ledgerHash, blockParams
            ).mapSuccess {
                LedgerParams(
                    element.getHashProperty("crypter"),
                    element.getStorageProperty("recalculationTime"),
                    element.getStorageProperty("recalculationTrigger"),
                    it
                )
            }

        }
}