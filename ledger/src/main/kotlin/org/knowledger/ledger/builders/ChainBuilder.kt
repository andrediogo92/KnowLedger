package org.knowledger.ledger.builders

import org.knowledger.collections.SortedList
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.ImmutableCoinbaseHeader
import org.knowledger.ledger.storage.config.chainid.ChainId
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput
import org.knowledger.ledger.storage.witness.ImmutableWitness

interface ChainBuilder {
    val chainHash: Hash
    val tag: Hash
    val chainId: ChainId

    fun block(
        transactions: SortedList<ImmutableTransaction>, coinbase: ImmutableCoinbase,
        blockHeader: ImmutableBlockHeader, merkleTree: ImmutableMerkleTree,
    ): ImmutableBlock

    fun blockheader(
        previousHash: Hash, merkleRoot: Hash, hash: Hash, seconds: Long, nonce: Long,
    ): ImmutableBlockHeader

    fun coinbase(
        header: ImmutableCoinbaseHeader, witnesses: SortedList<ImmutableWitness>,
        merkleTree: ImmutableMerkleTree,
    ): ImmutableCoinbase

    fun coinbaseHeader(
        hash: Hash, payout: Payout, merkleRoot: Hash,
        difficulty: Difficulty, blockheight: Long, extraNonce: Long,
    ): ImmutableCoinbaseHeader

    fun merkletree(
        collapsedTree: List<Hash>, levelIndex: List<Int>,
    ): ImmutableMerkleTree

    fun transaction(
        publicKey: EncodedPublicKey, physicalData: PhysicalData, signature: ByteArray, hash: Hash,
    ): ImmutableTransaction

    fun transaction(data: PhysicalData): ImmutableTransaction

    fun transactionOutput(
        payout: Payout, newIndex: Int, newTransaction: Hash, previousBlock: Hash,
        previousIndex: Int, previousTransaction: Hash,
    ): ImmutableTransactionOutput

    fun witness(
        transactionOutputs: SortedList<ImmutableTransactionOutput>,
        previousWitnessIndex: Int, prevCoinbase: Hash,
        publicKey: EncodedPublicKey, hash: Hash, payout: Payout,
    ): ImmutableWitness

    fun data(bytes: ByteArray): LedgerData

    fun toBytes(ledgerData: LedgerData): ByteArray
}
