package org.knowledger.agent.messaging

import org.knowledger.agent.core.ontologies.block.concepts.JBlock
import org.knowledger.agent.core.ontologies.block.concepts.JBlockHeader
import org.knowledger.agent.core.ontologies.block.concepts.JCoinbase
import org.knowledger.agent.core.ontologies.block.concepts.JMerkleTree
import org.knowledger.agent.core.ontologies.block.concepts.JTransactionOutput
import org.knowledger.agent.core.ontologies.ledger.concepts.JBlockParams
import org.knowledger.agent.core.ontologies.ledger.concepts.JChainId
import org.knowledger.agent.core.ontologies.ledger.concepts.JLedgerId
import org.knowledger.agent.core.ontologies.transaction.concepts.JPhysicalData
import org.knowledger.agent.core.ontologies.transaction.concepts.JTransaction
import org.knowledger.ledger.builders.ChainBuilder
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.core.misc.base64Decoded
import org.knowledger.ledger.core.misc.base64DecodedToHash
import org.knowledger.ledger.core.misc.base64Encoded
import org.knowledger.ledger.core.misc.mapToSet
import org.knowledger.ledger.core.misc.mapToSortedSet
import org.knowledger.ledger.core.misc.toBytes
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.data.Difficulty
import org.knowledger.ledger.data.GeoCoords
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.data.PhysicalData
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
        hashes = collapsedTree.map(Hash::base64Encoded),
        levelIndex = levelIndex
    )

fun BlockHeader.toJadeBlockHeader(): JBlockHeader =
    JBlockHeader(
        chainId = chainId.toJadeChainId(),
        hash = hash.base64Encoded(),
        merkleRoot = merkleRoot.base64Encoded(),
        previousHash = previousHash.base64Encoded(),
        params = params.toJadeBlockParams(),
        seconds = seconds,
        nonce = nonce
    )

fun ChainId.toJadeChainId(): JChainId =
    JChainId(
        tag = tag.base64Encoded(),
        hash = hash.base64Encoded(),
        ledger = ledgerHash.base64Encoded()
    )

fun BlockParams.toJadeBlockParams(): JBlockParams =
    JBlockParams(
        blockMemSize = blockMemSize,
        blockLength = blockLength
    )

fun LedgerId.toJadeLedgerId(): JLedgerId =
    JLedgerId(
        id = tag,
        hash = hash.base64Encoded()
    )


fun Coinbase.toJadeCoinbase(): JCoinbase =
    JCoinbase(
        payoutTXO = transactionOutputs
            .mapToSet(TransactionOutput::toJadeTransactionOutput),
        payout = payout.toString(),
        hashId = hash.base64Encoded(),
        formula = null,
        difficulty = difficulty.toBytes().base64Encoded(),
        blockheight = blockheight
    )

fun Coinbase.toJadeCoinbase(
    formula: String
): JCoinbase =
    JCoinbase(
        payoutTXO = transactionOutputs
            .mapToSet(TransactionOutput::toJadeTransactionOutput),
        payout = payout.toString(),
        hashId = hash.base64Encoded(),
        formula = formula,
        difficulty = difficulty.toBytes().base64Encoded(),
        blockheight = blockheight
    )

private fun TransactionOutput.toJadeTransactionOutput(): JTransactionOutput =
    JTransactionOutput(
        pubkey = publicKey.base64Encoded(),
        hashId = hash.base64Encoded(),
        prevCoinbase = previousCoinbase.base64Encoded(),
        payout = payout.toString(),
        tx = transactionHashes.map {
            it.base64Encoded()
        }.toSet()
    )


fun Transaction.toJadeTransaction(
    builder: ChainBuilder,
    withChainId: Boolean = false
): JTransaction =
    JTransaction(
        transactionId = hash.base64Encoded(),
        publicKey = publicKey.base64Encoded(),
        data = data.toJadePhysicalData(builder),
        signature = signature.base64Encoded(),
        ledgerId = if (withChainId) builder.chainId.toJadeChainId() else null
    )

fun PhysicalData.toJadePhysicalData(builder: ChainBuilder): JPhysicalData {
    return JPhysicalData(
        data = builder.toBytes(data).base64Encoded(),
        seconds = instant.epochSecond,
        nanos = instant.nano,
        latitude = coords.latitude.toString(),
        longitude = coords.longitude.toString(),
        altitude = coords.altitude.toString()
    )
}


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
        collapsedTree = hashes
            .map(String::base64DecodedToHash),
        levelIndex = levelIndex
    )

fun JBlockHeader.fromJadeBlockHeader(
    builder: ChainBuilder
): BlockHeader =
    builder.blockheader(
        previousHash = previousHash.base64DecodedToHash(),
        hash = hash.base64DecodedToHash(),
        merkleRoot = merkleRoot.base64DecodedToHash(),
        seconds = seconds,
        nonce = nonce
    )

fun JLedgerId.fromJadeLedgerId(): LedgerId =
    LedgerId(
        id,
        hash.base64DecodedToHash()
    )


fun JCoinbase.fromJadeCoinbase(
    builder: ChainBuilder
): Coinbase =
    builder.coinbase(
        transactionOutputs = payoutTXO.mapToSet {
            it.fromJadeTransactionOutput(builder)
        },
        payout = Payout(BigDecimal(payout)),
        hash = hashId.base64DecodedToHash(),
        difficulty = Difficulty(BigInteger(difficulty.base64Decoded())),
        blockheight = blockheight
    )

private fun JTransactionOutput.fromJadeTransactionOutput(
    builder: ChainBuilder
): TransactionOutput =
    builder.transactionOutput(
        publicKey = pubkey.toPublicKey(),
        prevCoinbase = prevCoinbase.base64DecodedToHash(),
        hash = hashId.base64DecodedToHash(),
        payout = Payout(BigDecimal(payout)),
        transactionSet = tx.mapToSet(String::base64DecodedToHash)
    )


fun JTransaction.fromJadeTransaction(
    builder: ChainBuilder
): Transaction =
    builder.transaction(
        publicKey.toPublicKey(),
        data.fromJadePhysicalData(builder),
        signature.base64Decoded(),
        transactionId.base64DecodedToHash()
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



