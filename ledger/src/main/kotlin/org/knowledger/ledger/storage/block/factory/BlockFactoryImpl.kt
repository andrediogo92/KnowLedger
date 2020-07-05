package org.knowledger.ledger.storage.block.factory

import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.block.header.factory.HashedBlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.transaction.factory.HashedTransactionFactory

internal class BlockFactoryImpl(
    private val coinbaseFactory: CoinbaseFactory,
    private val transactionFactory: HashedTransactionFactory,
    private val blockHeaderFactory: HashedBlockHeaderFactory,
    private val merkleTreeFactory: MerkleTreeFactory
) : BlockFactory {
    override fun create(
        transactions: MutableSortedList<MutableTransaction>,
        coinbase: MutableCoinbase,
        header: MutableBlockHeader,
        merkleTree: MutableMerkleTree
    ): BlockImpl =
        BlockImpl(
            innerTransactions = transactions, coinbase = coinbase,
            header = header, merkleTree = merkleTree
        )

    override fun create(
        other: MutableBlock
    ): BlockImpl =
        BlockImpl(
            innerTransactions = other.innerTransactions,
            coinbase = other.coinbase,
            header = other.header,
            merkleTree = other.merkleTree
        )

    override fun create(
        block: Block
    ): MutableBlock =
        BlockImpl(
            innerTransactions = block.transactions.map {
                transactionFactory.create(it)
            }.toMutableSortedListFromPreSorted(),
            coinbase = coinbaseFactory.create(block.coinbase),
            header = blockHeaderFactory.create(block.header),
            merkleTree = merkleTreeFactory.create(block.merkleTree)
        )
}