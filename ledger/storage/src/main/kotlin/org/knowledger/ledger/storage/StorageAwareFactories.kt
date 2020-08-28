package org.knowledger.ledger.storage

import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.block.factory.BlockFactory
import org.knowledger.ledger.storage.block.factory.BlockFactoryImpl
import org.knowledger.ledger.storage.block.factory.StorageAwareBlockFactory
import org.knowledger.ledger.storage.block.header.factory.BlockHeaderFactory
import org.knowledger.ledger.storage.block.header.factory.StorageAwareBlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactoryImpl
import org.knowledger.ledger.storage.coinbase.factory.StorageAwareCoinbaseFactory
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.coinbase.header.factory.StorageAwareCoinbaseHeaderFactory
import org.knowledger.ledger.storage.config.block.factory.BlockParamsFactory
import org.knowledger.ledger.storage.config.block.factory.StorageAwareBlockParamsFactory
import org.knowledger.ledger.storage.config.chainid.factory.ChainIdFactory
import org.knowledger.ledger.storage.config.chainid.factory.StorageAwareChainIdFactory
import org.knowledger.ledger.storage.config.coinbase.factory.CoinbaseParamsFactory
import org.knowledger.ledger.storage.config.coinbase.factory.StorageAwareCoinbaseParamsFactory
import org.knowledger.ledger.storage.config.ledger.factory.LedgerParamsFactory
import org.knowledger.ledger.storage.config.ledger.factory.StorageAwareLedgerParamsFactory
import org.knowledger.ledger.storage.merkletree.StorageAwareMerkleTreeFactory
import org.knowledger.ledger.storage.pools.block.BlockPoolFactory
import org.knowledger.ledger.storage.pools.block.BlockPoolFactoryImpl
import org.knowledger.ledger.storage.pools.transaction.factory.PoolTransactionFactory
import org.knowledger.ledger.storage.pools.transaction.factory.PoolTransactionFactoryImpl
import org.knowledger.ledger.storage.pools.transaction.factory.TransactionPoolFactory
import org.knowledger.ledger.storage.pools.transaction.factory.TransactionPoolFactoryImpl
import org.knowledger.ledger.storage.transaction.factory.StorageAwareTransactionFactory
import org.knowledger.ledger.storage.transaction.factory.TransactionFactory
import org.knowledger.ledger.storage.transaction.output.factory.StorageAwareTransactionOutputFactory
import org.knowledger.ledger.storage.transaction.output.factory.TransactionOutputFactory
import org.knowledger.ledger.storage.witness.factory.StorageAwareWitnessFactory
import org.knowledger.ledger.storage.witness.factory.WitnessFactory

internal class StorageAwareFactories : Factories {
    override val blockHeaderFactory: BlockHeaderFactory =
        StorageAwareBlockHeaderFactory()
    override val coinbaseHeaderFactory: CoinbaseHeaderFactory =
        StorageAwareCoinbaseHeaderFactory()
    override val merkleTreeFactory: MerkleTreeFactory =
        StorageAwareMerkleTreeFactory()
    override val transactionFactory: TransactionFactory =
        StorageAwareTransactionFactory()
    override val transactionOutputFactory: TransactionOutputFactory =
        StorageAwareTransactionOutputFactory()
    override val witnessFactory: WitnessFactory =
        StorageAwareWitnessFactory()
    override val coinbaseFactory: CoinbaseFactory = StorageAwareCoinbaseFactory(
        CoinbaseFactoryImpl(coinbaseHeaderFactory, merkleTreeFactory, witnessFactory)
    )
    override val blockFactory: BlockFactory = StorageAwareBlockFactory(
        BlockFactoryImpl(
            coinbaseFactory, coinbaseHeaderFactory, transactionFactory,
            blockHeaderFactory, merkleTreeFactory
        )
    )


    override val blockParamsFactory: BlockParamsFactory =
        StorageAwareBlockParamsFactory()
    override val chainIdFactory: ChainIdFactory =
        StorageAwareChainIdFactory()
    override val coinbaseParamsFactory: CoinbaseParamsFactory =
        StorageAwareCoinbaseParamsFactory()
    override val ledgerParamsFactory: LedgerParamsFactory =
        StorageAwareLedgerParamsFactory()


    override val blockPoolFactory: BlockPoolFactory =
        BlockPoolFactoryImpl()
    override val poolTransactionFactory: PoolTransactionFactory =
        PoolTransactionFactoryImpl()
    override val transactionPoolFactory: TransactionPoolFactory =
        TransactionPoolFactoryImpl(poolTransactionFactory)
}