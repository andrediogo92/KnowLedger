package org.knowledger.ledger.config.adapters

import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

internal object BlockParamsStorageAdapter : ServiceStorageAdapter<BlockParams> {
    override val id: String
        get() = "BlockParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "blockMemorySize" to StorageType.INTEGER,
            "blockLength" to StorageType.INTEGER
        )

    override fun store(
        toStore: BlockParams, session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "blockMemorySize", toStore.blockMemorySize
            ).setStorageProperty(
                "blockLength", toStore.blockLength
            )


    override fun load(
        ledgerHash: Hash,
        element: StorageElement
    ): Outcome<BlockParams, LedgerFailure> =
        tryOrLedgerUnknownFailure {
            Outcome.Ok(
                BlockParams(
                    element.getStorageProperty("blockMemorySize"),
                    element.getStorageProperty("blockLength")
                )
            )
        }

}