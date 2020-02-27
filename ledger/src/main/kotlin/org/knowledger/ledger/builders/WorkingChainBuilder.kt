package org.knowledger.ledger.builders

import kotlinx.serialization.PolymorphicSerializer
import org.knowledger.ledger.adapters.AdapterManager
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.LedgerData
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.block.BlockImpl
import org.knowledger.ledger.storage.blockheader.HashedBlockHeaderImpl
import org.knowledger.ledger.storage.coinbase.HashedCoinbaseImpl
import org.knowledger.ledger.storage.transaction.HashedTransactionImpl
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import java.security.PublicKey
import java.util.*

internal data class WorkingChainBuilder(
    internal val adapterManager: AdapterManager,
    internal val ledgerInfo: LedgerInfo,
    override val chainId: ChainId,
    internal val identity: Identity
) : ChainBuilder {
    override val chainHash: Hash
        get() = chainId.hash
    override val tag: Tag
        get() = chainId.tag

    override fun block(
        transactions: SortedSet<Transaction>,
        coinbase: Coinbase,
        blockHeader: BlockHeader,
        merkleTree: MerkleTree
    ): Block =
        BlockImpl(
            transactions = transactions.toSortedSet(),
            coinbase = coinbase,
            header = blockHeader,
            merkleTree = merkleTree
        )

    override fun blockheader(
        previousHash: Hash,
        merkleRoot: Hash,
        hash: Hash,
        seconds: Long,
        nonce: Long
    ): BlockHeader =
        HashedBlockHeaderImpl(
            chainId = chainId,
            previousHash = previousHash,
            blockParams = ledgerInfo.ledgerParams.blockParams,
            merkleRoot = merkleRoot,
            hash = hash,
            seconds = seconds,
            nonce = nonce,
            encoder = ledgerInfo.encoder,
            hasher = ledgerInfo.hasher
        )

    override fun coinbase(
        transactionOutputs: Set<TransactionOutput>,
        payout: Payout, difficulty: Difficulty,
        blockheight: Long, extraNonce: Long, hash: Hash
    ): Coinbase =
        HashedCoinbaseImpl(
            transactionOutputs = transactionOutputs.toMutableSet(),
            payout = payout, difficulty = difficulty,
            blockheight = blockheight,
            ledgerInfo = ledgerInfo,
            extraNonce = extraNonce,
            hash = hash
        )

    override fun merkletree(
        collapsedTree: List<Hash>,
        levelIndex: List<Int>
    ): MerkleTree =
        MerkleTreeImpl(
            collapsedTree = collapsedTree,
            levelIndex = levelIndex,
            hasher = ledgerInfo.hasher
        )

    override fun transactionOutput(
        transactionSet: Set<Hash>,
        prevCoinbase: Hash,
        publicKey: PublicKey, hash: Hash,
        payout: Payout
    ): TransactionOutput =
        HashedTransactionOutputImpl(
            previousCoinbase = prevCoinbase,
            publicKey = publicKey,
            hash = hash, payout = payout,
            transactionSet = transactionSet.toMutableSet(),
            hasher = ledgerInfo.hasher,
            encoder = ledgerInfo.encoder
        )

    override fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): Transaction =
        HashedTransactionImpl(
            publicKey = publicKey, hash = hash,
            data = physicalData, signature = signature
        )

    override fun transaction(
        data: PhysicalData
    ): Transaction =
        HashedTransactionImpl(
            identity = identity, data = data,
            hasher = ledgerInfo.hasher,
            encoder = ledgerInfo.encoder
        )

    override fun data(bytes: ByteArray): LedgerData =
        ledgerInfo.encoder.load(
            adapterManager.findAdapter(tag)!!.serializer,
            bytes
        )

    override fun toBytes(ledgerData: LedgerData): ByteArray =
        ledgerInfo.encoder.dump(
            PolymorphicSerializer(LedgerData::class),
            ledgerData
        )
}