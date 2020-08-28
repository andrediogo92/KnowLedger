package org.knowledger.ledger.storage.block.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.collections.mutableSortedListOf
import org.knowledger.collections.toMutableSortedListFromPreSorted
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.block.header.factory.BlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.transaction.factory.TransactionFactory

@OptIn(ExperimentalSerializationApi::class)
internal class BlockFactoryImpl(
    private val coinbaseFactory: CoinbaseFactory,
    private val coinbaseHeaderFactory: CoinbaseHeaderFactory,
    private val transactionFactory: TransactionFactory,
    private val blockHeaderFactory: BlockHeaderFactory,
    private val merkleTreeFactory: MerkleTreeFactory,
) : BlockFactory {

    override fun create(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): BlockImpl {
        val coinbase = coinbaseFactory.create(
            coinbaseHeaderFactory.create(coinbaseParams, hashers, encoder),
            merkleTreeFactory.create(hashers), mutableSortedListOf()
        )
        return create(
            blockHeaderFactory.create(chainHash, previousHash, blockParams, hashers, encoder),
            coinbase, merkleTreeFactory.create(hashers), mutableSortedListOf()
        )
    }

    override fun create(
        blockHeader: MutableBlockHeader, coinbase: MutableCoinbase,
        merkleTree: MutableMerkleTree, transactions: MutableSortedList<MutableTransaction>,
    ): BlockImpl = BlockImpl(blockHeader, coinbase, merkleTree, transactions)

    override fun create(other: MutableBlock): BlockImpl = create(other as Block)

    override fun create(block: Block): BlockImpl = with(block) {
        create(
            blockHeaderFactory.create(block.blockHeader),
            coinbaseFactory.create(block.coinbase), merkleTreeFactory.create(block.merkleTree),
            transactions.map(transactionFactory::create).toMutableSortedListFromPreSorted()
        )
    }
}