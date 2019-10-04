package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.block.loadBlockByImpl
import org.knowledger.ledger.storage.block.store

object BlockStorageAdapter : LedgerStorageAdapter<Block> {
    override val id: String
        get() = "Block"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "data" to StorageType.SET,
            "payout" to StorageType.LINK,
            "header" to StorageType.LINK,
            "merkleTree" to StorageType.LINK
        )

    override fun store(
        toStore: Block,
        session: ManagedSession
    ): StorageElement =
        toStore.store(session)


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Block, LoadFailure> =
        element.loadBlockByImpl(ledgerHash)
}