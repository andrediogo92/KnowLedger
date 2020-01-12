package org.knowledger.ledger.service.adapters

import org.knowledger.ledger.config.adapters.CoinbaseParamsStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerIdStorageAdapter
import org.knowledger.ledger.config.adapters.LedgerParamsStorageAdapter
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.intoHandle
import org.knowledger.ledger.results.mapFailure
import org.knowledger.ledger.results.tryOrHandleUnknownFailure
import org.knowledger.ledger.results.zip
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
                LedgerIdStorageAdapter.persist(
                    toStore.ledgerId, session
                )
            ).setLinked(
                "ledgerParams",
                LedgerParamsStorageAdapter.persist(
                    toStore.ledgerParams, session
                )
            ).setLinked(
                "coinbaseParams",
                CoinbaseParamsStorageAdapter.persist(
                    toStore.coinbaseParams, session
                )
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
                LedgerIdStorageAdapter.load(
                    ledgerHash, ledger
                ),
                LedgerParamsStorageAdapter.load(
                    ledgerHash, ledgerP
                ),
                CoinbaseParamsStorageAdapter.load(
                    ledgerHash, coinbaseParams
                )
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