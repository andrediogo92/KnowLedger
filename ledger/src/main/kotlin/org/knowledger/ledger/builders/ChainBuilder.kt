package org.knowledger.ledger.builders

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.Witness
import java.security.PublicKey

interface ChainBuilder {
    val chainHash: Hash
    val tag: Tag
    val chainId: ChainId

    fun block(
        transactions: MutableSortedList<Transaction>,
        coinbase: Coinbase, blockHeader: BlockHeader,
        merkleTree: MerkleTree
    ): Block

    fun blockheader(
        previousHash: Hash, merkleRoot: Hash,
        hash: Hash, seconds: Long, nonce: Long
    ): BlockHeader

    fun coinbase(
        witnesses: MutableSortedList<Witness>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, extraNonce: Long, hash: Hash
    ): Coinbase

    fun merkletree(
        collapsedTree: List<Hash>, levelIndex: List<Int>
    ): MerkleTree

    fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): Transaction

    fun transaction(
        data: PhysicalData
    ): Transaction

    fun transactionOutput(
        payout: Payout, newIndex: Int,
        newTransaction: Hash, previousBlock: Hash,
        previousIndex: Int, previousTransaction: Hash
    ): TransactionOutput

    fun witness(
        transactionOutputs: MutableSortedList<TransactionOutput>,
        previousWitnessIndex: Int, prevCoinbase: Hash,
        publicKey: EncodedPublicKey, hash: Hash,
        payout: Payout
    ): Witness

    fun data(bytes: ByteArray): LedgerData

    fun toBytes(ledgerData: LedgerData): ByteArray
}
