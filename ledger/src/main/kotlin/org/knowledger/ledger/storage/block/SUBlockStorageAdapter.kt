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
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.adapters.LedgerStorageAdapter
import org.knowledger.ledger.storage.block.factory.BlockFactory

internal class SUBlockStorageAdapter(
    private val blockFactory: BlockFactory,
    private val transactionStorageAdapter: LedgerStorageAdapter<MutableTransaction>,
    private val coinbaseStorageAdapter: LedgerStorageAdapter<MutableCoinbase>,
    private val blockHeaderStorageAdapter: LedgerStorageAdapter<MutableBlockHeader>,
    private val merkleTreeStorageAdapter: LedgerStorageAdapter<MutableMerkleTree>
) : LedgerStorageAdapter<MutableBlock> {
    override val id: String
        get() = "Block"

    override val properties: Map<String, StorageType>
        get() = mapOf(
            "transactions" to StorageType.LIST,
            "payout" to StorageType.LINK,
            "header" to StorageType.LINK,
            "merkleTree" to StorageType.LINK
        )

    override fun store(
        toStore: MutableBlock,
        session: ManagedSession
    ): StorageElement =
        session
            .newInstance(id)
            .setElementList(
                "transactions",
                toStore.innerTransactions.map {
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
    ): Outcome<MutableBlock, LoadFailure> =
        tryOrLoadUnknownFailure {
            val payout = element.getLinked("payout")
            val header = element.getLinked("header")
            val merkleTree = element.getLinked("merkleTree")
            zip(
                element
                    .getElementList("transactions")
                    .asSequence()
                    .map {
                        transactionStorageAdapter.load(
                            ledgerHash, it
                        )
                    }.allValues(),
                coinbaseStorageAdapter.load(
                    ledgerHash, payout
                ),
                blockHeaderStorageAdapter.load(
                    ledgerHash, header
                ),
                merkleTreeStorageAdapter.load(
                    ledgerHash, merkleTree
                )
            ) { data, coinbase, header, merkleTree ->
                blockFactory.create(
                    data.toMutableSortedListFromPreSorted(),
                    coinbase, header, merkleTree
                )
            }
        }
}