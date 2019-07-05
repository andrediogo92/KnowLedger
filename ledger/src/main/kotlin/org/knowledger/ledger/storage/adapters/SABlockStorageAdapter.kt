package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.mapSuccess
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.StorageAwareBlock

object SABlockStorageAdapter : LedgerStorageAdapter<StorageAwareBlock> {
    override val id: String
        get() = BlockStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = BlockStorageAdapter.properties

    override fun store(
        toStore: StorageAwareBlock,
        session: NewInstanceSession
    ): StorageElement =
        toStore.id?.element
            ?: SUBlockStorageAdapter
                .store(toStore.block, session)
                .also {
                    toStore.id = it.identity
                }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageAwareBlock, LoadFailure> =
        SUBlockStorageAdapter
            .load(ledgerHash, element)
            .mapSuccess { block ->
                StorageAwareBlock(block).also {
                    it.id = element.identity
                }
            }
}