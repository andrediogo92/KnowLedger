package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.config.adapters.loadCoinbaseParams
import org.knowledger.ledger.config.adapters.loadLedgerId
import org.knowledger.ledger.config.adapters.loadLedgerParams
import org.knowledger.ledger.config.adapters.persist
import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapFailure
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.results.intoHandle
import org.knowledger.ledger.results.tryOrHandleUnknownFailure
import org.knowledger.ledger.service.handles.LedgerHandle
import org.knowledger.ledger.service.handles.builder.LedgerConfig

internal object LedgerConfigStorageAdapter : HandleStorageAdapter {

    override val id: String
        get() = "LedgerConfig"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "ledgerId" to StorageType.LINK,
            "ledgerParams" to StorageType.LINK,
            "coinbaseParams" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerConfig, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setLinked(
                "ledgerId",
                toStore.ledgerId.persist(session)
            ).setLinked(
                "ledgerParams",
                toStore.ledgerParams.persist(session)
            ).setLinked(
                "coinbaseParams",
                toStore.coinbaseParams.persist(session)
            )


    @Suppress("NAME_SHADOWING")
    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        tryOrHandleUnknownFailure {
            val ledger = element.getLinked("ledgerId")
            val ledgerP = element.getLinked("ledgerParams")
            val coinbaseParams = element.getLinked("coinbaseParams")
            zip(
                ledger.loadLedgerId(ledgerHash),
                ledgerP.loadLedgerParams(ledgerHash),
                coinbaseParams.loadCoinbaseParams(ledgerHash)
            ) { ledgerId, ledgerParams, coinbaseParams ->
                LedgerConfig(
                    ledgerId,
                    ledgerParams,
                    coinbaseParams
                )
            }.mapFailure {
                it.intoHandle()
            }
        }
}