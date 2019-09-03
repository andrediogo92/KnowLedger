package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.block.SABlockStorageAdapter
import org.knowledger.ledger.storage.block.SUBlockStorageAdapter
import org.knowledger.ledger.storage.block.StorageAwareBlock

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
        session: NewInstanceSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlock ->
                SABlockStorageAdapter.store(toStore, session)
            is BlockImpl ->
                SUBlockStorageAdapter.store(toStore, session)
            else -> deadCode()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Block, LoadFailure> =
        SABlockStorageAdapter.load(ledgerHash, element)
}