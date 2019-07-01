package pt.um.masb.ledger.service.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.flatMapSuccess
import pt.um.masb.common.results.mapFailure
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.config.adapters.CoinbaseParamsStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerIdStorageAdapter
import pt.um.masb.ledger.config.adapters.LedgerParamsStorageAdapter
import pt.um.masb.ledger.results.intoHandle
import pt.um.masb.ledger.results.tryOrHandleUnknownFailure
import pt.um.masb.ledger.service.LedgerConfig
import pt.um.masb.ledger.service.handles.LedgerHandle

object LedgerConfigStorageAdapter : HandleStorageAdapter {

    override val id: String
        get() = "LedgerConfig"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "ledgerId" to StorageType.LINK,
            "ledgerParams" to StorageType.LINK,
            "coinbaseParams" to StorageType.LINK
        )

    override fun store(
        toStore: LedgerConfig, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setLinked(
                "ledgerId", LedgerIdStorageAdapter,
                toStore.ledgerId, session
            )
            setLinked(
                "ledgerParams", LedgerParamsStorageAdapter,
                toStore.ledgerParams, session
            )
            setLinked(
                "coinbaseParams", CoinbaseParamsStorageAdapter,
                toStore.coinbaseParams, session
            )
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LedgerHandle.Failure> =
        tryOrHandleUnknownFailure {
            val ledger = element.getLinked("ledgerId")
            val ledgerP = element.getLinked("ledgerParams")
            val coinbaseParams = element.getLinked("coinbaseParams")
            lateinit var ledgerId: LedgerId
            lateinit var ledgerParams: LedgerParams
            LedgerIdStorageAdapter.load(ledgerHash, ledger)
                .flatMapSuccess {
                    ledgerId = it
                    LedgerParamsStorageAdapter.load(
                        ledgerHash, ledgerP
                    )
                }.flatMapSuccess {
                    ledgerParams = it
                    CoinbaseParamsStorageAdapter.load(
                        ledgerHash, coinbaseParams
                    )
                }.flatMapSuccess {
                    Outcome.Ok(
                        LedgerConfig(
                            ledgerId,
                            ledgerParams,
                            it
                        )
                    )
                }.mapFailure {
                    it.intoHandle()
                }
        }
}