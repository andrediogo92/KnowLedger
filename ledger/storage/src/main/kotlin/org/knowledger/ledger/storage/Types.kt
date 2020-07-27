package org.knowledger.ledger.storage

import org.knowledger.ledger.core.PhysicalData
import org.knowledger.ledger.core.data.DataFormula
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.GeoCoords
import org.knowledger.ledger.core.data.HashSerializable
import org.knowledger.ledger.core.data.LedgerContract
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MutableMerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.block.MutableBlock
import org.knowledger.ledger.storage.block.header.HashedBlockHeader
import org.knowledger.ledger.storage.block.header.MutableHashedBlockHeader
import org.knowledger.ledger.storage.cache.StorageAware
import org.knowledger.ledger.storage.coinbase.Coinbase
import org.knowledger.ledger.storage.coinbase.MutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.HashedCoinbaseHeader
import org.knowledger.ledger.storage.coinbase.header.MutableHashedCoinbaseHeader
import org.knowledger.ledger.storage.config.LedgerId
import org.knowledger.ledger.storage.config.block.BlockParams
import org.knowledger.ledger.storage.config.chainid.ChainId
import org.knowledger.ledger.storage.config.coinbase.CoinbaseParams
import org.knowledger.ledger.storage.config.ledger.LedgerParams
import org.knowledger.ledger.storage.pools.transaction.MutableTransactionPool
import org.knowledger.ledger.storage.pools.transaction.PoolTransaction
import org.knowledger.ledger.storage.pools.transaction.TransactionPool
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.MutableHashedTransaction
import org.knowledger.ledger.storage.transaction.output.TransactionOutput
import org.knowledger.ledger.storage.witness.HashedWitness
import org.knowledger.ledger.storage.witness.MutableHashedWitness

//Service related structures
typealias Identity = Identity

//Primitive Structures
typealias Payout = Payout
typealias Difficulty = Difficulty
typealias LedgerData = LedgerData
typealias SelfInterval = SelfInterval
typealias GeoCoords = GeoCoords
typealias DataFormula = DataFormula
typealias DefaultDiff = DefaultDiff
typealias PhysicalData = PhysicalData

//Primary Data Structures
typealias Block = Block
typealias BlockHeader = HashedBlockHeader
typealias Coinbase = Coinbase
typealias CoinbaseHeader = HashedCoinbaseHeader
typealias MerkleTree = MerkleTree
typealias PoolTransaction = PoolTransaction
typealias Transaction = HashedTransaction
typealias TransactionOutput = TransactionOutput
typealias TransactionPool = TransactionPool
typealias Witness = HashedWitness
typealias LedgerContract = LedgerContract

//Primitive Configuration Structures
typealias HashSerializable = HashSerializable
typealias StorageAware = StorageAware
typealias ChainId = ChainId
typealias LedgerId = LedgerId
typealias BlockParams = BlockParams
typealias CoinbaseParams = CoinbaseParams
typealias LedgerParams = LedgerParams

//Primary Mutable Structures
typealias MutableBlock = MutableBlock
typealias MutableBlockHeader = MutableHashedBlockHeader
typealias MutableCoinbase = MutableCoinbase
typealias MutableCoinbaseHeader = MutableHashedCoinbaseHeader
typealias MutableMerkleTree = MutableMerkleTree
typealias MutableTransaction = MutableHashedTransaction
typealias MutableTransactionPool = MutableTransactionPool
typealias MutableWitness = MutableHashedWitness

