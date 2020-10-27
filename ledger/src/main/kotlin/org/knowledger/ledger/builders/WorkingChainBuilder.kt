package org.knowledger.ledger.builders

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.PolymorphicSerializer
import org.knowledger.collections.SortedList
import org.knowledger.collections.toSortedListFromPreSorted
import org.knowledger.ledger.chain.LedgerInfo
import org.knowledger.ledger.chain.PersistenceContext
import org.knowledger.ledger.core.PhysicalData
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.service.Identity
import org.knowledger.ledger.crypto.storage.ImmutableMerkleTree
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

@OptIn(ExperimentalSerializationApi::class)
internal data class WorkingChainBuilder(
    internal val context: PersistenceContext,
    internal val ledgerInfo: LedgerInfo,
    override val chainId: ChainId,
    internal val identity: Identity,
) : ChainBuilder {
    override val chainHash: Hash get() = chainId.hash
    override val tag: Hash get() = chainId.rawTag

    override fun block(
        transactions: SortedList<ImmutableTransaction>, coinbase: ImmutableCoinbase,
        blockHeader: ImmutableBlockHeader, merkleTree: ImmutableMerkleTree,
    ): ImmutableBlock = ImmutableBlock(
        blockHeader, coinbase, merkleTree, transactions.toSortedListFromPreSorted(),
        transactions.sumBy(ImmutableTransaction::approximateSize)
    )

    override fun blockheader(
        previousHash: Hash, merkleRoot: Hash, hash: Hash, seconds: Long, nonce: Long,
    ): ImmutableBlockHeader = ImmutableBlockHeader(
        chainHash, hash, merkleRoot, previousHash,
        chainId.blockParams.immutableCopy(), seconds, nonce
    )

    override fun coinbase(
        header: ImmutableCoinbaseHeader, witnesses: SortedList<ImmutableWitness>,
        merkleTree: ImmutableMerkleTree,
    ): ImmutableCoinbase = ImmutableCoinbase(header, merkleTree, witnesses)

    override fun coinbaseHeader(
        hash: Hash, payout: Payout, merkleRoot: Hash,
        difficulty: Difficulty, blockheight: Long, extraNonce: Long,
    ): ImmutableCoinbaseHeader = ImmutableCoinbaseHeader(
        hash, merkleRoot, payout, blockheight,
        difficulty, extraNonce, chainId.coinbaseParams.immutableCopy()
    )

    override fun merkletree(
        collapsedTree: List<Hash>, levelIndex: List<Int>,
    ): ImmutableMerkleTree =
        ImmutableMerkleTree(ledgerInfo.hashers, collapsedTree, levelIndex)

    override fun transaction(
        publicKey: EncodedPublicKey, physicalData: PhysicalData, signature: ByteArray, hash: Hash,
    ): ImmutableTransaction =
        ImmutableTransaction(hash, EncodedSignature(signature), publicKey, physicalData)

    override fun transaction(data: PhysicalData): ImmutableTransaction =
        context.transactionFactory.create(
            identity.privateKey, identity.publicKey, data, ledgerInfo.hashers, ledgerInfo.encoder,
        ).immutableCopy()

    override fun transactionOutput(
        payout: Payout, newIndex: Int, newTransaction: Hash,
        previousBlock: Hash, previousIndex: Int, previousTransaction: Hash,
    ): ImmutableTransactionOutput = ImmutableTransactionOutput(
        payout, newTransaction, newIndex, previousBlock, previousIndex, previousTransaction
    )

    override fun witness(
        transactionOutputs: SortedList<ImmutableTransactionOutput>,
        previousWitnessIndex: Int, prevCoinbase: Hash,
        publicKey: EncodedPublicKey, hash: Hash, payout: Payout,
    ): ImmutableWitness = ImmutableWitness(
        hash, publicKey, previousWitnessIndex, prevCoinbase, payout, transactionOutputs,
    )

    override fun data(bytes: ByteArray): LedgerData =
        ledgerInfo.encoder.decodeFromByteArray(context.findAdapter(tag)!!.serializer, bytes)

    override fun toBytes(ledgerData: LedgerData): ByteArray =
        ledgerInfo.encoder.encodeToByteArray(
            PolymorphicSerializer(LedgerData::class), ledgerData
        )
}