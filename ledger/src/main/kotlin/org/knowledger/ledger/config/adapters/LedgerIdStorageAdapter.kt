package org.knowledger.ledger.config.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object LedgerIdStorageAdapter : ServiceStorageAdapter<LedgerId> {
    override val id: String
        get() = "LedgerId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.STRING,
            "hashId" to StorageType.HASH
        )

    override fun store(
        toStore: LedgerId, session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setStorageProperty("tag", toStore.tag)
            setHashProperty("hashId", toStore.hashId)
        }


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<LedgerId, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val hash =
                element.getHashProperty("hashId")

            assert(hash.contentEquals(ledgerHash))


            Outcome.Ok(
                LedgerId(
                    element.getStorageProperty("tag"),
                    ledgerHash
                )
            )
        }
}