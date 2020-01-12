package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.block.SABlockStorageAdapter
import org.knowledger.ledger.storage.block.SUBlockStorageAdapter
import org.knowledger.ledger.storage.block.StorageAwareBlock

internal class BlockStorageAdapter(
    private val suBlockStorageAdapter: SUBlockStorageAdapter,
    private val saBlockStorageAdapter: SABlockStorageAdapter
) : LedgerStorageAdapter<Block>,
    SchemaProvider by suBlockStorageAdapter {

    override fun store(
        toStore: Block,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlock ->
                saBlockStorageAdapter.store(toStore, session)
            is BlockImpl ->
                suBlockStorageAdapter.store(toStore, session)
            else -> deadCode()
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<Block, LoadFailure> =
        saBlockStorageAdapter.load(ledgerHash, element)
}