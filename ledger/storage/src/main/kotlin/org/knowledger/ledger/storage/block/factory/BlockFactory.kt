package org.knowledger.ledger.storage.block.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.Hashers
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockParams
import org.knowledger.ledger.storage.CloningFactory
import org.knowledger.ledger.storage.CoinbaseParams
import org.knowledger.ledger.storage.MutableBlock
import org.knowledger.ledger.storage.MutableBlockHeader
import org.knowledger.ledger.storage.MutableCoinbase
import org.knowledger.ledger.storage.MutableMerkleTree
import org.knowledger.ledger.storage.MutableTransaction

@OptIn(ExperimentalSerializationApi::class)
interface BlockFactory : CloningFactory<MutableBlock> {
    fun create(
        chainHash: Hash, previousHash: Hash, blockParams: BlockParams,
        coinbaseParams: CoinbaseParams, hashers: Hashers, encoder: BinaryFormat,
    ): MutableBlock

    fun create(
        blockHeader: MutableBlockHeader, coinbase: MutableCoinbase,
        merkleTree: MutableMerkleTree, transactions: MutableSortedList<MutableTransaction>,
    ): MutableBlock

    fun create(block: Block): MutableBlock
}