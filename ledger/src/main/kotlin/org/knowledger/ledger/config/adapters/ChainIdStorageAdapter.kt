package org.knowledger.ledger.config.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

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