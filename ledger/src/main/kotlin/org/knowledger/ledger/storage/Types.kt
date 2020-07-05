package org.knowledger.ledger.storage

import org.knowledger.ledger.core.base.storage.LedgerContract
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.crypto.storage.MutableMerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.block.MutableBlock
import org.knowledger.ledger.storage.block.header.HashedBlockHeader
import org.knowledger.ledger.storage.block.header.MutableHashedBlockHeader
import org.knowledger.ledger.storage.coinbase.Coinbase
import org.knowledger.ledger.storage.coinbase.MutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.HashedCoinbaseHeader
import org.knowledger.ledger.storage.coinbase.header.MutableHashedCoinbaseHeader
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.MutableHashedTransaction
import org.knowledger.ledger.storage.transaction.output.TransactionOutput
import org.knowledger.ledger.storage.witness.HashedWitness
import org.knowledger.ledger.storage.witness.MutableHashedWitness

typealias Block = Block
typealias BlockHeader = HashedBlockHeader
typealias Coinbase = Coinbase
typealias CoinbaseHeader = HashedCoinbaseHeader
typealias MerkleTree = MerkleTree
typealias Transaction = HashedTransaction
typealias TransactionOutput = TransactionOutput
typealias Witness = HashedWitness

internal typealias LedgerContract = LedgerContract
internal typealias MutableBlock = MutableBlock
internal typealias MutableBlockHeader = MutableHashedBlockHeader
internal typealias MutableCoinbase = MutableCoinbase
internal typealias MutableCoinbaseHeader = MutableHashedCoinbaseHeader
internal typealias MutableMerkleTree = MutableMerkleTree
internal typealias MutableTransaction = MutableHashedTransaction
internal typealias MutableWitness = MutableHashedWitness