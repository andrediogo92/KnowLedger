package org.knowledger.ledger.storage

import org.knowledger.ledger.core.base.storage.LedgerContract
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.TransactionOutput
import org.knowledger.ledger.storage.witness.HashedWitness

typealias Block = Block
typealias BlockHeader = HashedBlockHeader
typealias Coinbase = HashedCoinbase
typealias MerkleTree = MerkleTree
typealias Transaction = HashedTransaction
typealias TransactionOutput = TransactionOutput
typealias Witness = HashedWitness
internal typealias LedgerContract = LedgerContract

