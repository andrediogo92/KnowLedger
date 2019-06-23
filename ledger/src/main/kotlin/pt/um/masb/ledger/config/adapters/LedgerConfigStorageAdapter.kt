package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.common.results.flatMapSuccess
import pt.um.masb.ledger.config.LedgerConfig
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.results.tryOrLoadUnknownFailure
import pt.um.masb.ledger.service.results.LoadFailure
import pt.um.masb.ledger.storage.adapters.LedgerStorageAdapter

object LedgerConfigStorageAdapter : LedgerStorageAdapter<LedgerConfig> {

    override val id: String
        get() = "LedgerConfig"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "ledgerId" to StorageType.LINK,
            "ledgerParams" to StorageType.LINK
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
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerConfig, LoadFailure> =
        tryOrLoadUnknownFailure {
            val ledger = element.getLinked("ledgerId")
            val ledgerParams = element.getLinked("ledgerParams")
            lateinit var ledgerId: LedgerId
            LedgerIdStorageAdapter
                .load(ledgerHash, ledger)
                .flatMapSuccess {
                    ledgerId = it
                    LedgerParamsStorageAdapter.load(
                        ledgerHash, ledgerParams
                    )
                }.flatMapSuccess {
                    Outcome.Ok(
                        LedgerConfig(
                            ledgerId,
                            it
                        )
                    )
                }
        }
}