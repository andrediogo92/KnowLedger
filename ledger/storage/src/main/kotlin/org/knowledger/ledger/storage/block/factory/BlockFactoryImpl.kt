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
import org.knowledger.ledger.storage.block.header.factory.BlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.transaction.factory.TransactionFactory

internal class BlockFactoryImpl(
    private val coinbaseFactory: CoinbaseFactory,
    private val transactionFactory: TransactionFactory,
    private val blockHeaderFactory: BlockHeaderFactory,
    private val merkleTreeFactory: MerkleTreeFactory
) : BlockFactory {
    override fun create(
        transactions: MutableSortedList<MutableTransaction>,
        coinbase: MutableCoinbase,
        header: MutableBlockHeader,
        merkleTree: MutableMerkleTree
    ): BlockImpl =
        BlockImpl(
            mutableTransactions = transactions, coinbase = coinbase,
            blockHeader = header, merkleTree = merkleTree
        )

    override fun create(
        other: MutableBlock
    ): BlockImpl =
        BlockImpl(
            mutableTransactions = other.mutableTransactions,
            coinbase = other.coinbase,
            blockHeader = other.blockHeader,
            merkleTree = other.merkleTree
        )

    override fun create(
        block: Block
    ): BlockImpl =
        BlockImpl(
            mutableTransactions = block.transactions.map {
                transactionFactory.create(it)
            }.toMutableSortedListFromPreSorted(),
            coinbase = coinbaseFactory.create(block.coinbase),
            blockHeader = blockHeaderFactory.create(block.blockHeader),
            merkleTree = merkleTreeFactory.create(block.merkleTree)
        )
}