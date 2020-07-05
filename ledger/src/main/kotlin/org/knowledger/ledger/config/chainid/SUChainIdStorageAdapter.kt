package org.knowledger.ledger.config.chainid

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.factory.ChainIdFactory
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal class SUChainIdStorageAdapter(
    private val chainIdFactory: ChainIdFactory
) : ServiceStorageAdapter<ChainId> {
    override val id: String
        get() = "ChainId"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "tag" to StorageType.HASH,
            "ledgerHash" to StorageType.HASH,
            "hash" to StorageType.HASH
        )


    override fun store(
        toStore: ChainId, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setHashProperty("tag", toStore.tag)
            .setHashProperty("ledgerHash", toStore.ledgerHash)
            .setHashProperty("hash", toStore.hash)


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<ChainId, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            val hash: Hash =
                element.getHashProperty("hash")

            val ledger: Hash =
                element.getHashProperty("ledgerHash")

            val tag: Hash =
                element.getHashProperty("tag")


            assert(ledger == ledgerHash)

            Outcome.Ok(
                chainIdFactory.create(
                    tag, ledger, hash
                )
            )
        }
}