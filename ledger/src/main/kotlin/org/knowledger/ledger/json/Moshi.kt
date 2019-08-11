package org.knowledger.ledger.json

import com.squareup.moshi.Moshi
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.StorageAwareChainId
import org.knowledger.ledger.config.chainid.StorageUnawareChainId
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.data.DummyData
import org.knowledger.ledger.service.pool.StorageAwareTransactionPool
import org.knowledger.ledger.service.pool.StorageUnawareTransactionPool
import org.knowledger.ledger.service.pool.TransactionPool
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.block.StorageAwareBlock
import org.knowledger.ledger.storage.block.StorageUnawareBlock
import org.knowledger.ledger.storage.blockheader.StorageAwareBlockHeader
import org.knowledger.ledger.storage.blockheader.StorageUnawareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageAwareCoinbase
import org.knowledger.ledger.storage.coinbase.StorageUnawareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTree
import org.knowledger.ledger.storage.merkletree.StorageUnawareMerkleTree


fun Moshi.Builder.addLedgerAdapters(
    ledgerDataTypes: Map<Class<out LedgerData>, String>
): Moshi.Builder {
    var ledgerAdapter = PolymorphicJsonAdapterFactory
        .of(LedgerData::class.java, "data_type")
        .withSubtype(DummyData::class.java, "Dummy")
    ledgerDataTypes.forEach { entry ->
        ledgerAdapter = ledgerAdapter.withSubtype(entry.key, entry.value)
    }
    return add(ledgerAdapter)
        .add(HashJsonAdapter())
        .add(PublicKeyJsonAdapter())
        .add(InstantJsonAdapter())
        .add(BigDecimalJsonAdapter())
        .add(BigIntegerJsonAdapter())
        .add(
            PolymorphicJsonAdapterFactory
                .of(Block::class.java, "block")
                .withSubtype(StorageAwareBlock::class.java, "StorageAware")
                .withSubtype(StorageUnawareBlock::class.java, "StorageUnaware")
        )
        .add(
            PolymorphicJsonAdapterFactory
                .of(BlockHeader::class.java, "block_header")
                .withSubtype(StorageAwareBlockHeader::class.java, "StorageAware")
                .withSubtype(StorageUnawareBlockHeader::class.java, "StorageUnaware")
        )
        .add(
            PolymorphicJsonAdapterFactory
                .of(Coinbase::class.java, "coinbase")
                .withSubtype(StorageAwareCoinbase::class.java, "StorageAware")
                .withSubtype(StorageUnawareCoinbase::class.java, "StorageUnaware")
        )
        .add(
            PolymorphicJsonAdapterFactory
                .of(MerkleTree::class.java, "merkle_tree")
                .withSubtype(StorageAwareMerkleTree::class.java, "StorageAware")
                .withSubtype(StorageUnawareMerkleTree::class.java, "StorageUnaware")
        )
        .add(
            PolymorphicJsonAdapterFactory
                .of(ChainId::class.java, "chain_id")
                .withSubtype(StorageAwareChainId::class.java, "StorageAware")
                .withSubtype(StorageUnawareChainId::class.java, "StorageUnaware")
        ).add(
            PolymorphicJsonAdapterFactory
                .of(TransactionPool::class.java, "transaction_pool")
                .withSubtype(StorageAwareTransactionPool::class.java, "StorageAware")
                .withSubtype(StorageUnawareTransactionPool::class.java, "StorageUnaware")
        )
        .add(SortedSetJsonAdapterFactory)

}
