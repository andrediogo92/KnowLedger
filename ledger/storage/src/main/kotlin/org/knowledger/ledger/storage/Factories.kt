package org.knowledger.ledger.storage

import org.knowledger.ledger.crypto.storage.MerkleTreeFactory
import org.knowledger.ledger.storage.block.factory.BlockFactory
import org.knowledger.ledger.storage.block.header.factory.BlockHeaderFactory
import org.knowledger.ledger.storage.coinbase.factory.CoinbaseFactory
import org.knowledger.ledger.storage.coinbase.header.factory.CoinbaseHeaderFactory
import org.knowledger.ledger.storage.config.block.factory.BlockParamsFactory
import org.knowledger.ledger.storage.config.chainid.factory.ChainIdFactory
import org.knowledger.ledger.storage.config.coinbase.factory.CoinbaseParamsFactory
import org.knowledger.ledger.storage.config.ledger.factory.LedgerParamsFactory
import org.knowledger.ledger.storage.pools.transaction.factory.PoolTransactionFactory
import org.knowledger.ledger.storage.pools.transaction.factory.TransactionPoolFactory
import org.knowledger.ledger.storage.transaction.factory.TransactionFactory
import org.knowledger.ledger.storage.transaction.output.factory.TransactionOutputFactory
import org.knowledger.ledger.storage.witness.factory.WitnessFactory

interface Factories {
    //Storage class factories
    val blockFactory: BlockFactory
    val blockHeaderFactory: BlockHeaderFactory
    val coinbaseFactory: CoinbaseFactory
    val coinbaseHeaderFactory: CoinbaseHeaderFactory
    val merkleTreeFactory: MerkleTreeFactory
    val transactionFactory: TransactionFactory
    val transactionOutputFactory: TransactionOutputFactory
    val witnessFactory: WitnessFactory

    //Config class factories
    val blockParamsFactory: BlockParamsFactory
    val chainIdFactory: ChainIdFactory
    val coinbaseParamsFactory: CoinbaseParamsFactory
    val ledgerParamsFactory: LedgerParamsFactory


    //Pool class factories
    val poolTransactionFactory: PoolTransactionFactory
    val transactionPoolFactory: TransactionPoolFactory
}