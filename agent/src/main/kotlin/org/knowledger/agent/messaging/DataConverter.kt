package org.knowledger.agent.messaging

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerializationStrategy
import org.knowledger.agent.core.ontologies.block.concepts.JBlock
import org.knowledger.agent.core.ontologies.block.concepts.JBlockHeader
import org.knowledger.agent.core.ontologies.block.concepts.JCoinbase
import org.knowledger.agent.core.ontologies.block.concepts.JMerkleTree
import org.knowledger.agent.core.ontologies.block.concepts.JTransactionOutput
import org.knowledger.agent.core.ontologies.ledger.concepts.JBlockParams
import org.knowledger.agent.core.ontologies.ledger.concepts.JChainId
import org.knowledger.agent.core.ontologies.ledger.concepts.JLedgerId
import org.knowledger.agent.core.ontologies.transaction.concepts.JHash
import org.knowledger.agent.core.ontologies.transaction.concepts.JPhysicalData
import org.knowledger.agent.core.ontologies.transaction.concepts.JTransaction
import org.knowledger.agent.data.CheckedData
import org.knowledger.agent.data.CheckedTransaction
import org.knowledger.base64.base64Decoded
import org.knowledger.base64.base64DecodedToHash
import org.knowledger.base64.base64Encoded
import org.knowledger.collections.mapToSet
import org.knowledger.collections.mapToSortedSet
import org.knowledger.ledger.builders.ChainBuilder
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.GeoCoords
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
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant


fun Block.toJadeBlock(chainBuilder: ChainBuilder): JBlock =
    JBlock(
        data = transactions.mapToSortedSet {
            it.toJadeTransaction(chainBuilder)
        },
        coinbase = coinbase.toJadeCoinbase(),
        header = header.toJadeBlockHeader(),
        merkleTree = merkleTree.toJadeMerkleTree()
    )

fun MerkleTree.toJadeMerkleTree(): JMerkleTree =
    JMerkleTree(
        hashes = collapsedTree.map(Hash::toJadeHash),
        levelIndex = levelIndex
    )

fun BlockHeader.toJadeBlockHeader(): JBlockHeader =
    JBlockHeader(
        chainId = chainId.toJadeChainId(),
        hash = hash.toJadeHash(),
        merkleRoot = merkleRoot.toJadeHash(),
        previousHash = previousHash.toJadeHash(),
        params = params.toJadeBlockParams(),
        seconds = seconds,
        nonce = nonce
    )

fun ChainId.toJadeChainId(): JChainId =
    JChainId(
        tag = tag.toJadeHash(),
        hash = hash.toJadeHash(),
        ledger = ledgerHash.toJadeHash()
    )

fun BlockParams.toJadeBlockParams(): JBlockParams =
    JBlockParams(
        blockMemSize = blockMemorySize,
        blockLength = blockLength
    )

fun LedgerId.toJadeLedgerId(): JLedgerId =
    JLedgerId(
        id = tag,
        hash = hash.toJadeHash()
    )


fun Coinbase.toJadeCoinbase(): JCoinbase =
    JCoinbase(
        payoutTXO = transactionOutputs
            .mapToSet(TransactionOutput::toJadeTransactionOutput),
        payout = payout.toString(),
        hashId = hash.toJadeHash(),
        formula = null,
        difficulty = JHash(difficulty.bytes.base64Encoded()),
        blockheight = blockheight
    )

fun Coinbase.toJadeCoinbase(
    formula: Hash
): JCoinbase =
    JCoinbase(
        payoutTXO = transactionOutputs
            .mapToSet(TransactionOutput::toJadeTransactionOutput),
        payout = payout.toString(),
        hashId = hash.toJadeHash(),
        formula = formula.toJadeHash(),
        difficulty = JHash(difficulty.bytes.base64Encoded()),
        blockheight = blockheight
    )

private fun TransactionOutput.toJadeTransactionOutput(): JTransactionOutput =
    JTransactionOutput(
        pubkey = publicKey.base64Encoded(),
        hashId = hash.toJadeHash(),
        prevCoinbase = previousCoinbase.toJadeHash(),
        payout = payout.toString(),
        tx = transactionHashes.mapToSet(Hash::toJadeHash)
    )


fun Transaction.toJadeTransaction(
    builder: ChainBuilder,
    withChainId: Boolean = false
): JTransaction =
    JTransaction(
        transactionId = hash.toJadeHash(),
        publicKey = publicKey.base64Encoded(),
        data = data.toJadePhysicalData(builder),
        signature = signature.encoded.base64Encoded(),
        ledgerId = if (withChainId) builder.chainId.toJadeChainId() else null
    )

fun PhysicalData.toJadePhysicalData(builder: ChainBuilder): JPhysicalData {
    return JPhysicalData(
        data = builder.toBytes(data).base64Encoded(),
        tag = builder.tag.toJadeHash(),
        seconds = instant.epochSecond,
        nanos = instant.nano,
        latitude = coords.latitude.toString(),
        longitude = coords.longitude.toString(),
        altitude = coords.altitude.toString()
    )
}

@Suppress("UNCHECKED_CAST")
fun <T : LedgerData> PhysicalData.toJadePhysicalData(
    serializer: SerializationStrategy<T>,
    encoder: BinaryFormat,
    tag: Tag
): JPhysicalData {
    return JPhysicalData(
        data = encoder.dump(serializer, data as T).base64Encoded(),
        tag = tag.toJadeHash(),
        seconds = instant.epochSecond,
        nanos = instant.nano,
        latitude = coords.latitude.toString(),
        longitude = coords.longitude.toString(),
        altitude = coords.altitude.toString()
    )
}

fun Hash.toJadeHash(): JHash =
    JHash(base64Encoded())


//Conversions from Jade Types

fun JBlock.fromJadeBlock(
    builder: ChainBuilder
): Block =
    builder.block(
        transactions = data.mapToSortedSet {
            it.fromJadeTransaction(builder)
        },
        coinbase = coinbase.fromJadeCoinbase(builder),
        blockHeader = header.fromJadeBlockHeader(builder),
        merkleTree = merkleTree.fromJadeMerkleTree(builder)
    )


fun JMerkleTree.fromJadeMerkleTree(
    builder: ChainBuilder
): MerkleTree =
    builder.merkletree(
        collapsedTree = hashes.map(JHash::fromJadeHash),
        levelIndex = levelIndex
    )

fun JBlockHeader.fromJadeBlockHeader(
    builder: ChainBuilder
): BlockHeader =
    builder.blockheader(
        previousHash = previousHash.fromJadeHash(),
        hash = hash.fromJadeHash(),
        merkleRoot = merkleRoot.fromJadeHash(),
        seconds = seconds,
        nonce = nonce
    )

fun JLedgerId.fromJadeLedgerId(): LedgerId =
    LedgerId(
        id,
        hash.fromJadeHash()
    )


fun JCoinbase.fromJadeCoinbase(
    builder: ChainBuilder
): Coinbase =
    builder.coinbase(
        transactionOutputs = payoutTXO.mapToSet {
            it.fromJadeTransactionOutput(builder)
        },
        payout = Payout(BigDecimal(payout)),
        hash = hashId.fromJadeHash(),
        difficulty = Difficulty(BigInteger(difficulty.hash.base64Decoded())),
        blockheight = blockheight
    )

private fun JTransactionOutput.fromJadeTransactionOutput(
    builder: ChainBuilder
): TransactionOutput =
    builder.transactionOutput(
        publicKey = EncodedPublicKey(pubkey.base64Decoded()).toPublicKey(),
        prevCoinbase = prevCoinbase.fromJadeHash(),
        hash = hashId.fromJadeHash(),
        payout = Payout(BigDecimal(payout)),
        transactionSet = tx.mapToSet(JHash::fromJadeHash)
    )


fun JTransaction.fromJadeTransaction(
    builder: ChainBuilder
): Transaction =
    builder.transaction(
        EncodedPublicKey(publicKey.base64Decoded()).toPublicKey(),
        data.fromJadePhysicalData(builder),
        signature.base64Decoded(),
        transactionId.fromJadeHash()
    )

fun JPhysicalData.fromJadePhysicalData(
    builder: ChainBuilder
): PhysicalData =
    PhysicalData(
        Instant.ofEpochSecond(seconds, nanos.toLong()),
        GeoCoords(
            BigDecimal(latitude),
            BigDecimal(longitude),
            BigDecimal(altitude)
        ),
        builder.data(data.base64Decoded())
    )

fun JHash.fromJadeHash(): Hash =
    hash.base64DecodedToHash()


fun Transaction.checked(tag: Tag): CheckedTransaction =
    CheckedTransaction(tag, this)

fun PhysicalData.checked(tag: Tag): CheckedData =
    CheckedData(tag, this)