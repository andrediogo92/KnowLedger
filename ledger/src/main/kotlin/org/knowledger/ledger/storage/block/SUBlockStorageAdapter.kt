package org.knowledger.ledger.storage.block

import org.knowledger.ledger.core.database.NewInstanceSession
import org.knowledger.ledger.core.database.StorageElement
import org.knowledger.ledger.core.database.StorageType
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.allValues
import org.knowledger.ledger.core.results.zip
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.BlockStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter

object SUBlockStorageAdapter : LedgerStorageAdapter<StorageUnawareBlock> {
    override val id: String
        get() = BlockStorageAdapter.id

    override val properties: Map<String, StorageType>
        get() = BlockStorageAdapter.properties

    override fun store(
        toStore: StorageUnawareBlock,
        session: NewInstanceSession
    ): StorageElement =
        session.newInstance(id).apply {
            setElementList(
                "data",
                toStore.data.map {
                    TransactionStorageAdapter.store(
                        it, session
                    )
                }
            )
            setLinked(
                "payout", CoinbaseStorageAdapter,
                toStore.coinbase, session
            )
            setLinked(
                "header", BlockHeaderStorageAdapter,
                toStore.header, session
            )
            setLinked(
                "merkleTree", MerkleTreeStorageAdapter,
                toStore.merkleTree, session
            )
        }


    override fun load(
        ledgerHash: Hash, element: StorageElement
    ): Outcome<StorageUnawareBlock, LoadFailure> =
        tryOrLoadUnknownFailure {
            zip(
                element
                    .getElementSet("data")
                    .asSequence()
                    .map {
                        TransactionStorageAdapter.load(
                            ledgerHash, it
                        )
                    }.allValues(),
                CoinbaseStorageAdapter.load(
                    ledgerHash,
                    element.getLinked(
                        "payout"
                    )
                ),
                BlockHeaderStorageAdapter.load(
                    ledgerHash,
                    element.getLinked(
                        "header"
                    )
                ),
                MerkleTreeStorageAdapter.load(
                    ledgerHash,
                    element.getLinked(
                        "merkleTree"
                    )
                )
            )
            { data, coinbase, header, merkleTree ->
                StorageUnawareBlock(
                    data.toSortedSet(),
                    coinbase,
                    header,
                    merkleTree
                )
            }
        }
}