package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.adapters.SchemaProvider
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.deadCode
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.blockheader.SABlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.SUBlockHeaderStorageAdapter
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader

internal class BlockHeaderStorageAdapter(
    private val suBlockHeaderStorageAdapter: SUBlockHeaderStorageAdapter,
    private val saBlockHeaderStorageAdapter: SABlockHeaderStorageAdapter
) : LedgerStorageAdapter<BlockHeader>,
    SchemaProvider by suBlockHeaderStorageAdapter {
    override fun store(
        toStore: BlockHeader,
        session: ManagedSession
    ): StorageElement =
        when (toStore) {
            is StorageAwareBlockHeader ->
                saBlockHeaderStorageAdapter.store(toStore, session)
            is HashedBlockHeaderImpl ->
                suBlockHeaderStorageAdapter.store(toStore, session)
            else -> deadCode()
        }

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<BlockHeader, LoadFailure> =
        saBlockHeaderStorageAdapter.load(ledgerHash, element)

}