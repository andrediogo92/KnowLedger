package org.knowledger.ledger.storage.block

import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.database.ManagedSession
import org.knowledger.ledger.database.StorageElement
import org.knowledger.ledger.database.StorageType
import org.knowledger.ledger.results.Outcome
import org.knowledger.ledger.results.allValues
import org.knowledger.ledger.results.tryOrLoadUnknownFailure
import org.knowledger.ledger.results.zip
import org.knowledger.ledger.service.results.LoadFailure
import org.knowledger.ledger.storage.adapters.BlockHeaderStorageAdapter
import org.knowledger.ledger.storage.adapters.CoinbaseStorageAdapter
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.adapters.MerkleTreeStorageAdapter
import org.knowledger.ledger.storage.adapters.TransactionStorageAdapter

internal class SUBlockStorageAdapter(
    private val transactionStorageAdapter: TransactionStorageAdapter,
    private val coinbaseStorageAdapter: CoinbaseStorageAdapter,
    private val blockHeaderStorageAdapter: BlockHeaderStorageAdapter,
    private val merkleTreeStorageAdapter: MerkleTreeStorageAdapter
) : LedgerStorageAdapter<BlockImpl> {
    override val id: String
        get() = "Block"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "data" to StorageType.LIST,
            "payout" to StorageType.LINK,
            "header" to StorageType.LINK,
            "merkleTree" to StorageType.LINK
        )

    override fun store(
        toStore: BlockImpl,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setElementList(
                "data",
                toStore.transactions.map {
                    transactionStorageAdapter.persist(
                        it, session
                    )
                }
            ).setLinked(
                "payout",
                coinbaseStorageAdapter.persist(
                    toStore.coinbase, session
                )
            ).setLinked(
                "header",
                blockHeaderStorageAdapter.persist(
                    toStore.header, session
                )
            ).setLinked(
                "merkleTree",
                merkleTreeStorageAdapter.persist(
                    toStore.merkleTree, session
                )
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
                    .getElementList("data")
                    .asSequence()
                    .map {
                        transactionStorageAdapter.load(
                            ledgerHash, it
                        )
                    }.allValues(),
                coinbaseStorageAdapter.load(
                    ledgerHash,
                    payout
                ),
                blockHeaderStorageAdapter.load(
                    ledgerHash,
                    header
                ),
                merkleTreeStorageAdapter.load(
                    ledgerHash,
                    merkleTree
                )
            )
            { data, coinbase, header, merkleTree ->
                BlockImpl(
                    data.toMutableSortedListFromPreSorted(), coinbase,
                    header, merkleTree
                )
            }
        }
}