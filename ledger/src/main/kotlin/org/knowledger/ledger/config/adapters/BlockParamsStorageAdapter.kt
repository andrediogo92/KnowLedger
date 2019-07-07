package org.knowledger.ledger.config.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.results.tryOrLedgerUnknownFailure
import org.knowledger.ledger.service.adapters.ServiceStorageAdapter
import org.knowledger.ledger.service.results.LedgerFailure

object BlockParamsStorageAdapter : ServiceStorageAdapter<BlockParams> {
    override val id: String
        get() = "BlockParams"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "blockMemSize" to StorageType.LONG,
            "blockLength" to StorageType.LONG
        )

    override fun store(
        toStore: BlockParams, session: NewInstanceSession
    ): StorageElement =
        session
            .newInstance(id)
            .setStorageProperty(
                "blockMemSize", toStore.blockMemSize
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
                    element.getStorageProperty("blockMemSize"),
                    element.getStorageProperty("blockLength")
                )
            )
        }

}