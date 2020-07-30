package org.knowledger.ledger.builders

import kotlinx.serialization.PolymorphicSerializer
import org.knowledger.collections.SortedList
import org.knowledger.collections.toSortedListFromPreSorted
import org.knowledger.ledger.core.PhysicalData
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
import org.knowledger.ledger.service.LedgerInfo
import org.knowledger.ledger.service.PersistenceContext
import org.knowledger.ledger.storage.Difficulty
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.block.header.ImmutableBlockHeader
import org.knowledger.ledger.storage.coinbase.ImmutableCoinbase
import org.knowledger.ledger.storage.coinbase.header.ImmutableCoinbaseHeader
import org.knowledger.ledger.storage.config.chainid.ChainId
import org.knowledger.ledger.storage.immutableCopy
import org.knowledger.ledger.storage.transaction.ImmutableTransaction
import org.knowledger.ledger.storage.transaction.output.ImmutableTransactionOutput
import org.knowledger.ledger.storage.witness.ImmutableWitness
import java.security.PublicKey

internal data class WorkingChainBuilder(
    internal val context: PersistenceContext,
    internal val ledgerInfo: LedgerInfo,
    override val chainId: ChainId,
    internal val identity: Identity
) : ChainBuilder {
    override val chainHash: Hash
        get() = chainId.hash
    override val tag: Hash
        get() = chainId.tag

    override fun block(
        transactions: SortedList<ImmutableTransaction>,
        coinbase: ImmutableCoinbase,
        blockHeader: ImmutableBlockHeader,
        merkleTree: ImmutableMerkleTree
    ): ImmutableBlock = ImmutableBlock(
        immutableTransactions = transactions.toSortedListFromPreSorted(),
        coinbase = coinbase, blockHeader = blockHeader, merkleTree = merkleTree,
        approximateSize = transactions.sumBy { it.approximateSize }
    )

    override fun blockheader(
        previousHash: Hash, merkleRoot: Hash,
        hash: Hash, seconds: Long, nonce: Long
    ): ImmutableBlockHeader = ImmutableBlockHeader(
        chainHash = chainHash,
        blockParams = chainId.blockParams.immutableCopy(),
        previousHash = previousHash, merkleRoot = merkleRoot,
        hash = hash, seconds = seconds, nonce = nonce
    )

    override fun coinbase(
        header: ImmutableCoinbaseHeader,
        witnesses: SortedList<ImmutableWitness>,
        merkleTree: ImmutableMerkleTree
    ): ImmutableCoinbase = ImmutableCoinbase(
        coinbaseHeader = header,
        immutableWitnesses = witnesses,
        merkleTree = merkleTree
    )

    override fun coinbaseHeader(
        hash: Hash, payout: Payout,
        merkleRoot: Hash, difficulty: Difficulty,
        blockheight: Long, extraNonce: Long
    ): ImmutableCoinbaseHeader =
        ImmutableCoinbaseHeader(
            hash = hash, payout = payout,
            coinbaseParams = chainId.coinbaseParams.immutableCopy(),
            merkleRoot = merkleRoot, difficulty = difficulty,
            blockheight = blockheight, extraNonce = extraNonce
        )

    override fun merkletree(
        collapsedTree: List<Hash>, levelIndex: List<Int>
    ): ImmutableMerkleTree = ImmutableMerkleTree(
        ledgerInfo.hashers, collapsedTree, levelIndex
    )

    override fun transaction(
        publicKey: PublicKey, physicalData: PhysicalData,
        signature: ByteArray, hash: Hash
    ): ImmutableTransaction =
        ImmutableTransaction(
            publicKey = publicKey, hash = hash,
            data = physicalData, signature = EncodedSignature(signature)
        )

    override fun transaction(data: PhysicalData): ImmutableTransaction =
        context.transactionFactory.create(
            privateKey = identity.privateKey,
            publicKey = identity.publicKey,
            data = data, encoder = ledgerInfo.encoder,
            hashers = ledgerInfo.hashers
        ).immutableCopy()

    override fun transactionOutput(
        payout: Payout, newIndex: Int,
        newTransaction: Hash, previousBlock: Hash,
        previousIndex: Int, previousTransaction: Hash
    ): ImmutableTransactionOutput =
        ImmutableTransactionOutput(
            payout = payout, txIndex = newIndex,
            tx = newTransaction,
            prevTxBlock = previousBlock,
            prevTxIndex = previousIndex,
            prevTx = previousTransaction
        )

    override fun witness(
        transactionOutputs: SortedList<ImmutableTransactionOutput>,
        previousWitnessIndex: Int, prevCoinbase: Hash,
        publicKey: EncodedPublicKey, hash: Hash,
        payout: Payout
    ): ImmutableWitness = ImmutableWitness(
        immutableTransactionOutputs = transactionOutputs,
        previousWitnessIndex = previousWitnessIndex,
        previousCoinbase = prevCoinbase,
        publicKey = publicKey, hash = hash, payout = payout
    )

    override fun data(bytes: ByteArray): LedgerData =
        ledgerInfo.encoder.load(
            context.findAdapter(tag)!!.serializer, bytes
        )

    override fun toBytes(ledgerData: LedgerData): ByteArray =
        ledgerInfo.encoder.dump(
            PolymorphicSerializer(LedgerData::class), ledgerData
        )
}