package org.knowledger.ledger.storage.adapters

import org.knowledger.common.database.NewInstanceSession
import org.knowledger.common.database.StorageElement
import org.knowledger.common.database.StorageType
import org.knowledger.common.hash.Hash
import org.knowledger.common.results.Outcome
import org.knowledger.common.results.allValues
import org.knowledger.common.results.zip
import org.knowledger.ledger.data.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.StorageUnawareBlock

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
                "coinbase", CoinbaseStorageAdapter,
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
                        "coinbase"
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