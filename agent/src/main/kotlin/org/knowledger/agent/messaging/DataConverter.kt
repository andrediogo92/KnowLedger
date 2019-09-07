package org.knowledger.agent.messaging

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
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
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.GeoCoords
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.base64Decoded
import org.knowledger.ledger.core.misc.base64DecodedToHash
import org.knowledger.ledger.core.misc.base64Encoded
import org.knowledger.ledger.core.misc.toBytes
import org.knowledger.ledger.core.misc.toPublicKey
import org.knowledger.ledger.crypto.storage.MerkleTree
import org.knowledger.ledger.storage.block.Block
import org.knowledger.ledger.storage.blockheader.HashedBlockHeader
import org.knowledger.ledger.storage.coinbase.HashedCoinbase
import org.knowledger.ledger.storage.transaction.HashedTransaction
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutput
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant


fun Block.toJadeBlock(): JBlock =
    JBlock(
        data = data
            .map(HashedTransaction::toJadeTransaction)
            .toSortedSet(),
        coinbase = coinbase.toJadeCoinbase(),
        header = header.toJadeBlockHeader(),
        merkleTree = merkleTree.toJadeMerkleTree()
    )

fun MerkleTree.toJadeMerkleTree(): JMerkleTree =
    JMerkleTree(
        hashes = collapsedTree.map(Hash::base64Encoded),
        levelIndex = levelIndex
    )

fun HashedBlockHeader.toJadeBlockHeader(): JBlockHeader =
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


fun HashedCoinbase.toJadeCoinbase(): JCoinbase =
    JCoinbase(
        payoutTXO = transactionOutputs
            .map(HashedTransactionOutput::toJadeTransactionOutput)
            .toSet(),
        payout = payout.toString(),
        hashId = hash.base64Encoded(),
        formula = null,
        difficulty = difficulty.toBytes().base64Encoded(),
        blockheight = blockHeight
    )

fun HashedCoinbase.toJadeCoinbase(
    formula: String
): JCoinbase =
    JCoinbase(
        payoutTXO = transactionOutputs
            .map(HashedTransactionOutput::toJadeTransactionOutput)
            .toSet(),
        payout = payout.toString(),
        hashId = hash.base64Encoded(),
        formula = formula,
        difficulty = difficulty.toBytes().base64Encoded(),
        blockheight = blockHeight
    )

private fun HashedTransactionOutput.toJadeTransactionOutput(): JTransactionOutput =
    JTransactionOutput(
        pubkey = publicKey.base64Encoded(),
        hashId = hash.base64Encoded(),
        prevCoinbase = previousCoinbase.base64Encoded(),
        payout = payout.toString(),
        tx = transactionHashes.map {
            it.base64Encoded()
        }.toSet()
    )


fun HashedTransaction.toJadeTransaction(): JTransaction =
    JTransaction(
        transactionId = hash.base64Encoded(),
        publicKey = publicKey.base64Encoded(),
        data = data.toJadePhysicalData(),
        signature = signature.base64Encoded(),
        ledgerId = null
    )

fun HashedTransaction.toJadeTransactionWithChain(
    chainId: ChainId
): JTransaction =
    JTransaction(
        transactionId = hash.base64Encoded(),
        publicKey = publicKey.base64Encoded(),
        data = data.toJadePhysicalData(),
        signature = signature.base64Encoded(),
        ledgerId = chainId.toJadeChainId()
    )


fun PhysicalData.toJadePhysicalData(): JPhysicalData {
    val byteStream = ByteArrayOutputStream(2048)
    ObjectOutputStream(byteStream).use {
        it.writeObject(data)
    }
    return JPhysicalData(
        data = byteStream.toByteArray().base64Encoded(),
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
        transactions = data.asSequence().map {
            it.fromJadeTransaction(builder)
        }.toSortedSet(),
        coinbase = coinbase.fromJadeCoinbase(builder),
        blockHeader = header.fromJadeBlockHeader(builder),
        merkleTree = merkleTree.fromJadeMerkleTree(builder)
    )


fun JMerkleTree.fromJadeMerkleTree(
    builder: ChainBuilder
): MerkleTree =
    builder.merkletree(
        collapsedTree = hashes
            .asSequence()
            .map(String::base64DecodedToHash)
            .toMutableList(),
        levelIndex = levelIndex as MutableList<Int>
    )

fun JBlockHeader.fromJadeBlockHeader(
    builder: ChainBuilder
): HashedBlockHeader =
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
): HashedCoinbase =
    builder.coinbase(
        transactionOutputs = payoutTXO
            .asSequence()
            .map {
                it.fromJadeTransactionOutput(builder)
            }
            .toSet(),
        payout = Payout(BigDecimal(payout)),
        hash = hashId.base64DecodedToHash(),
        difficulty = Difficulty(BigInteger(difficulty.base64Decoded())),
        blockheight = blockheight
    )

private fun JTransactionOutput.fromJadeTransactionOutput(
    builder: ChainBuilder
): HashedTransactionOutput =
    builder.transactionOutput(
        publicKey = pubkey.toPublicKey(),
        prevCoinbase = prevCoinbase.base64DecodedToHash(),
        hash = hashId.base64DecodedToHash(),
        payout = Payout(BigDecimal(payout)),
        transactionSet = tx
            .asSequence()
            .map(String::base64DecodedToHash)
            .toSet()
    )


fun JTransaction.fromJadeTransaction(
    builder: ChainBuilder
): HashedTransaction =
    builder.transaction(
        publicKey.toPublicKey(),
        data.fromJadePhysicalData(),
        signature.base64Decoded(),
        transactionId.base64DecodedToHash()
    )

fun JPhysicalData.fromJadePhysicalData(
): PhysicalData =
    ObjectInputStream(
        ByteArrayInputStream(data.base64Decoded())
    ).use {
        it.readObject() as LedgerData
    }.let {
        PhysicalData(
            Instant.ofEpochSecond(seconds, nanos.toLong()),
            GeoCoords(
                BigDecimal(latitude),
                BigDecimal(longitude),
                BigDecimal(altitude)
            ),
            it
        )
    }



