package org.knowledger.ledger.storage.adapters

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.blockheader.loadBlockHeaderByImpl
import org.knowledger.ledger.storage.blockheader.store

internal object BlockHeaderStorageAdapter : LedgerStorageAdapter<BlockHeader> {
    override val id: String
        get() = "BlockHeader"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "chainId" to StorageType.LINK,
            "hash" to StorageType.HASH,
            "merkleRoot" to StorageType.HASH,
            "previousHash" to StorageType.HASH,
            "blockParams" to StorageType.LINK,
            "seconds" to StorageType.LONG,
            "nonce" to StorageType.LONG
        )

    override fun store(
        toStore: BlockHeader,
        session: ManagedSession
    ): StorageElement =
        toStore.store(session)

    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<BlockHeader, LoadFailure> =
        element.loadBlockHeaderByImpl(ledgerHash)

}