package org.knowledger.ledger.storage.block

import org.knowledger.ledger.core.database.ManagedSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.allValues
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.loadBlockHeader
import org.knowledger.ledger.storage.adapters.loadCoinbase
import org.knowledger.ledger.storage.adapters.loadMerkleTree
import org.knowledger.ledger.storage.adapters.loadTransaction
import org.knowledger.ledger.storage.adapters.persist

internal object SUBlockStorageAdapter : LedgerStorageAdapter<BlockImpl> {
    override val id: String
        get() = BlockStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = BlockStorageAdapter.properties

    override fun store(
        toStore: BlockImpl,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setElementSet(
                "data", toStore.data.map {
                    it.persist(session)
                }.toSet()
            ).setLinked(
                "payout", toStore.coinbase.persist(session)
            ).setLinked(
                "header", toStore.header.persist(session)
            ).setLinked(
                "merkleTree", toStore.merkleTree.persist(session)
            )


    @Suppress("NAME_SHADOWING")
    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<BlockImpl, LoadFailure> =
        tryOrLoadUnknownFailure {
            val payout = element.getLinked("payout")
            val header = element.getLinked("header")
            val merkleTree = element.getLinked("merkleTree")
            zip(
                element
                    .getElementSet("data")
                    .asSequence()
                    .map {
                        it.loadTransaction(ledgerHash)
                    }.allValues(),
                payout.loadCoinbase(
                    ledgerHash
                ),
                header.loadBlockHeader(
                    ledgerHash
                ),
                merkleTree.loadMerkleTree(
                    ledgerHash
                )
            )
            { data, coinbase, header, merkleTree ->
                BlockImpl(
                    data.toSortedSet(), coinbase,
                    header, merkleTree
                )
            }
        }
}