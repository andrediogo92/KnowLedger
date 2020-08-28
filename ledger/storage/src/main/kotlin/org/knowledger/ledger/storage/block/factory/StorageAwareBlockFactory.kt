package org.knowledger.ledger.storage.block.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.block.StorageAwareBlockImpl

@OptIn(ExperimentalSerializationApi::class)
internal class StorageAwareBlockFactory(
    private val blockFactory: BlockFactory,
) : BlockFactory {
    private fun createSA(block: MutableBlock): StorageAwareBlockImpl =
        StorageAwareBlockImpl(block)

    override fun create(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): MutableBlock = createSA(
        blockFactory.create(
            chainHash, previousHash, blockParams, coinbaseParams, hashers, encoder
        )
    )

    override fun create(
        blockHeader: MutableBlockHeader, coinbase: MutableCoinbase,
        merkleTree: MutableMerkleTree, transactions: MutableSortedList<MutableTransaction>,
    ): StorageAwareBlockImpl =
        createSA(blockFactory.create(blockHeader, coinbase, merkleTree, transactions))

    override fun create(other: MutableBlock): StorageAwareBlockImpl =
        createSA(blockFactory.create(other as Block))

    override fun create(block: Block): StorageAwareBlockImpl =
        createSA(blockFactory.create(block))
}