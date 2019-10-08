package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import java.security.PublicKey
import java.util.*

interface ChainBuilder {
    val chainHash: Hash
    val tag: Tag
    val chainId: ChainId

    fun block(
        transactions: SortedSet<Transaction>,
        coinbase: Coinbase, blockHeader: BlockHeader,
        merkleTree: MerkleTree
    ): Block

    fun blockheader(
        previousHash: Hash, merkleRoot: Hash,
        hash: Hash, seconds: Long, nonce: Long
    ): BlockHeader

    fun coinbase(
        transactionOutputs: Set<TransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, hash: Hash
    ): Coinbase

    fun merkletree(
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MerkleTree

    fun transactionOutput(
        transactionSet: Set<Hash>, prevCoinbase: Hash,
        publicKey: PublicKey, hash: Hash, payout: Payout
    ): TransactionOutput

    fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): Transaction

    fun transaction(
        data: PhysicalData
    ): Transaction

    fun data(bytes: ByteArray): LedgerData

    fun toBytes(ledgerData: LedgerData): ByteArray
}
