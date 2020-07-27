package org.knowledger.ledger.storage

import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.crypto.storage.MerkleTreeFactoryImpl
import org.knowledger.ledger.storage.block.factory.BlockFactory
import org.knowledger.ledger.storage.block.factory.BlockFactoryImpl
import org.knowledger.ledger.storage.block.header.factory.BlockHeaderFactory
import org.knowledger.ledger.storage.block.header.factory.HashedBlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactoryImpl
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.coinbase.header.factory.HashedCoinbaseHeaderFactory
import org.knowledger.ledger.storage.config.block.factory.BlockParamsFactory
import org.knowledger.ledger.storage.config.block.factory.BlockParamsFactoryImpl
import org.knowledger.ledger.storage.config.chainid.factory.ChainIdFactory
import org.knowledger.ledger.storage.config.chainid.factory.ChainIdFactoryImpl
import org.knowledger.ledger.storage.config.coinbase.factory.CoinbaseParamsFactory
import org.knowledger.ledger.storage.config.coinbase.factory.CoinbaseParamsFactoryImpl
import org.knowledger.ledger.storage.config.ledger.factory.LedgerParamsFactory
import org.knowledger.ledger.storage.config.ledger.factory.LedgerParamsFactoryImpl
import org.knowledger.ledger.storage.pools.transaction.factory.PoolTransactionFactory
import org.knowledger.ledger.storage.pools.transaction.factory.PoolTransactionFactoryImpl
import org.knowledger.ledger.storage.pools.transaction.factory.TransactionPoolFactory
import org.knowledger.ledger.storage.pools.transaction.factory.TransactionPoolFactoryImpl
import org.knowledger.ledger.storage.transaction.factory.HashedTransactionFactory
import org.knowledger.ledger.storage.transaction.factory.TransactionFactory
import org.knowledger.ledger.storage.transaction.output.factory.TransactionOutputFactory
import org.knowledger.ledger.storage.transaction.output.factory.TransactionOutputFactoryImpl
import org.knowledger.ledger.storage.witness.factory.HashedWitnessFactory
import org.knowledger.ledger.storage.witness.factory.WitnessFactory

internal class StorageUnawareFactories : Factories {
    override val blockHeaderFactory: BlockHeaderFactory = HashedBlockHeaderFactory()
    override val coinbaseHeaderFactory: CoinbaseHeaderFactory = HashedCoinbaseHeaderFactory()
    override val merkleTreeFactory: MerkleTreeFactory = MerkleTreeFactoryImpl()
    override val transactionFactory: TransactionFactory = HashedTransactionFactory()
    override val transactionOutputFactory: TransactionOutputFactory = TransactionOutputFactoryImpl()
    override val witnessFactory: WitnessFactory = HashedWitnessFactory()
    override val coinbaseFactory: CoinbaseFactory = CoinbaseFactoryImpl(
        coinbaseHeaderFactory, merkleTreeFactory, witnessFactory
    )
    override val blockFactory: BlockFactory = BlockFactoryImpl(
        coinbaseFactory, transactionFactory, blockHeaderFactory, merkleTreeFactory
    )


    override val blockParamsFactory: BlockParamsFactory = BlockParamsFactoryImpl()
    override val chainIdFactory: ChainIdFactory = ChainIdFactoryImpl()
    override val coinbaseParamsFactory: CoinbaseParamsFactory = CoinbaseParamsFactoryImpl()
    override val ledgerParamsFactory: LedgerParamsFactory = LedgerParamsFactoryImpl()


    override val poolTransactionFactory: PoolTransactionFactory = PoolTransactionFactoryImpl()
    override val transactionPoolFactory: TransactionPoolFactory
        get() = TransactionPoolFactoryImpl(poolTransactionFactory)
}