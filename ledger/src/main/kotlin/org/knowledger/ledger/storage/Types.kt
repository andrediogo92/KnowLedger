package org.knowledger.ledger.storage

import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput

typealias Transaction = HashedTransaction
typealias TransactionOutput = HashedTransactionOutput
typealias MerkleTree = MerkleTree
typealias Coinbase = HashedCoinbase
typealias BlockHeader = HashedBlockHeader
typealias Block = Block
typealias PhysicalData = PhysicalData