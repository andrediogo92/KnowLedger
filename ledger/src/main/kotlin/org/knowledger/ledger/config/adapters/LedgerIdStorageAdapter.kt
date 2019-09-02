package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object LedgerIdStorageAdapter : ServiceStorageAdapter<LedgerId> {
    override val id: String
        get() = "LedgerId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.STRING,
            "hash" to StorageType.HASH
        )

    override fun store(
        toStore: LedgerId, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty("tag", toStore.tag)
            .setHashProperty("hash", toStore.hash)


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerId, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val hash =
                element.getHashProperty("hash")

            assert(hash == ledgerHash)

            Outcome.Ok(
                LedgerId(
                    element.getStorageProperty("tag"),
                    ledgerHash
                )
            )
        }
}