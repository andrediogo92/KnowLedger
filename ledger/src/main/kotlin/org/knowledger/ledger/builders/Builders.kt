package org.knowledger.ledger.builders

import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.results.Outcome
import org.knowledger.ledger.core.results.mapSuccess
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.results.BuilderFailure
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.block.StorageUnawareBlock
import org.knowledger.ledger.storage.blockheader.StorageUnawareBlockHeader
import org.knowledger.ledger.storage.coinbase.StorageUnawareCoinbase
import org.knowledger.ledger.storage.merkletree.StorageUnawareMerkleTree
import java.security.PublicKey
import java.util.*

inline fun LedgerConfig.withConfig(
    chainId: ChainId,
    init: LedgerConfig.ByConfigBuilder.() -> Unit
): Outcome<LedgerConfig.ByConfigBuilder, BuilderFailure> =
    withConfig(chainId).mapSuccess {
        it.init()
        it
    }

fun LedgerConfig.withConfig(
    chainId: ChainId
): Outcome<LedgerConfig.ByConfigBuilder, BuilderFailure> =
    LedgerConfig
        .ByConfigBuilder(this, chainId)
        .build()

fun LedgerConfig.ByConfigBuilder.block(
    transactions: SortedSet<Transaction>,
    coinbase: Coinbase,
    blockHeader: BlockHeader,
    merkleTree: MerkleTree
): Block =
    StorageUnawareBlock(
        transactions, coinbase,
        blockHeader, merkleTree
    )

fun LedgerConfig.ByConfigBuilder.blockheader(
    previousHash: Hash,
    merkleRoot: Hash,
    hash: Hash,
    seconds: Long,
    nonce: Long
): BlockHeader =
    StorageUnawareBlockHeader(
        chainId = chainId,
        hasher = ledgerContainer.hasher,
        previousHash = previousHash,
        params = ledgerContainer.ledgerParams.blockParams,
        merkleRoot = merkleRoot,
        hash = hash,
        seconds = seconds,
        nonce = nonce
    )

fun LedgerConfig.ByConfigBuilder.coinbase(
    payoutTXO: MutableSet<TransactionOutput>,
    payout: Payout,
    hash: Hash,
    difficulty: Difficulty,
    blockheight: Long
): Coinbase =
    StorageUnawareCoinbase(
        payoutTXO = payoutTXO,
        payout = payout,
        hash = hash,
        difficulty = difficulty,
        blockheight = blockheight,
        hasher = ledgerContainer.hasher,
        formula = ledgerContainer.formula,
        coinbaseParams = ledgerContainer.coinbaseParams
    )

fun LedgerConfig.ByConfigBuilder.merkletree(
    collapsedTree: MutableList<Hash>,
    levelIndex: MutableList<Int>
): MerkleTree =
    StorageUnawareMerkleTree(
        hasher = ledgerContainer.hasher,
        collapsedTree = collapsedTree,
        levelIndex = levelIndex
    )

fun LedgerConfig.ByConfigBuilder.transaction(
    publicKey: PublicKey, physicalData: PhysicalData,
    signature: ByteArray, transactionId: Hash
): Transaction =
    Transaction(
        chainId = chainId, publicKey = publicKey,
        data = physicalData, signatureInternal = signature,
        hash = transactionId, hasher = ledgerContainer.hasher
    )

internal fun <T : Builder<*, *>> T.uninitialized(
    parameter: String
): BuilderFailure.ParameterUninitialized =
    BuilderFailure.ParameterUninitialized(
        "$parameter not initialized for ${this::class.simpleName}"
    )
