package pt.um.masb.ledger.config.adapters

import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.StorageType
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.results.Outcome
import pt.um.masb.ledger.config.ChainId
import pt.um.masb.ledger.results.tryOrLedgerUnknownFailure
import pt.um.masb.ledger.service.adapters.ServiceStorageAdapter
import pt.um.masb.ledger.service.results.LedgerFailure

object ChainIdStorageAdapter : ServiceStorageAdapter<ChainId> {
    override val id: String
        get() = "ChainId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.STRING,
            "ledgerHash" to StorageType.HASH,
            "hashId" to StorageType.HASH
        )

    override fun store(
        toStore: ChainId, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty("tag", toStore.tag)
            setHashProperty("ledgerHash", toStore.ledgerHash)
            setHashProperty("hashId", toStore.hashId)
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainId, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val hash =
                element.getHashProperty("hashId")

            val ledger =
                element.getHashProperty("ledgerHash")

            assert(ledger.contentEquals(ledgerHash))

            Outcome.Ok(
                ChainId(
                    element.getStorageProperty("tag"),
                    ledger,
                    hash
                )
            )
        }
}