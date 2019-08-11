package org.knowledger.agent.messaging

import org.knowledger.agent.messaging.block.ontology.concepts.JBlock
import org.knowledger.agent.messaging.block.ontology.concepts.JBlockHeader
import org.knowledger.agent.messaging.block.ontology.concepts.JCoinbase
import org.knowledger.agent.messaging.block.ontology.concepts.JMerkleTree
import org.knowledger.agent.messaging.block.ontology.concepts.JTransactionOutput
import org.knowledger.agent.messaging.ledger.ontology.concepts.JBlockParams
import org.knowledger.agent.messaging.ledger.ontology.concepts.JChainId
import org.knowledger.agent.messaging.ledger.ontology.concepts.JLedgerId
import org.knowledger.agent.messaging.transaction.ontology.concepts.JPhysicalData
import org.knowledger.agent.messaging.transaction.ontology.concepts.JTransaction
import org.knowledger.ledger.builders.block
import org.knowledger.ledger.builders.blockheader
import org.knowledger.ledger.builders.coinbase
import org.knowledger.ledger.builders.merkletree
import org.knowledger.ledger.builders.transaction
import org.knowledger.ledger.config.BlockParams
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.misc.base64Decode
import org.knowledger.ledger.core.misc.base64DecodeToHash
import org.knowledger.ledger.core.misc.base64Encode
import org.knowledger.ledger.core.misc.getStringFromKey
import org.knowledger.ledger.core.misc.stringToPublicKey
import org.knowledger.ledger.data.GeoCoords
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.LedgerConfig
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant


fun Block.convertToJadeBlock(): JBlock =
    JBlock(
        data = data
            .map(Transaction::convertToJadeTransaction)
            .toSortedSet(),
        coinbase = coinbase.convertToJadeCoinbase(),
        header = header.convertToJadeBlockHeader(),
        merkleTree = merkleTree.convertToJadeMerkleTree()
    )

fun MerkleTree.convertToJadeMerkleTree(): JMerkleTree =
    JMerkleTree(
        hashes = nakedTree.map(Hash::base64Encode),
        levelIndex = levelIndexes
    )

fun BlockHeader.convertToJadeBlockHeader(): JBlockHeader =
    JBlockHeader(
        chainId = chainId.convertToJadeChainId(),
        hash = hashId.base64Encode(),
        merkleRoot = merkleRoot.base64Encode(),
        previousHash = previousHash.base64Encode(),
        params = params.convertToJadeBlockParams(),
        seconds = seconds,
        nonce = nonce
    )

fun ChainId.convertToJadeChainId(): JChainId =
    JChainId(
        tag = tag,
        hash = hashId.base64Encode(),
        ledger = ledgerHash.base64Encode()
    )

fun BlockParams.convertToJadeBlockParams(): JBlockParams =
    JBlockParams(
        blockMemSize = blockMemSize,
        blockLength = blockLength
    )

fun LedgerId.convertToJadeLedgerId(): JLedgerId =
    JLedgerId(
        id = tag,
        hash = hashId.base64Encode()
    )


fun Coinbase.convertToJadeCoinbase(): JCoinbase =
    JCoinbase(
        payoutTXO = payoutTXO
            .map(TransactionOutput::convertToJadeTransactionOutput)
            .toSet(),
        payout = payout.toString(),
        hashId = hashId.base64Encode(),
        formula = null,
        difficulty = difficulty.bytes.base64Encode(),
        blockheight = blockheight
    )

fun Coinbase.convertToJadeCoinbase(
    formula: String
): JCoinbase =
    JCoinbase(
        payoutTXO = payoutTXO
            .map(TransactionOutput::convertToJadeTransactionOutput)
            .toSet(),
        payout = payout.toString(),
        hashId = hashId.base64Encode(),
        formula = formula,
        difficulty = difficulty.bytes.base64Encode(),
        blockheight = blockheight
    )

private fun TransactionOutput.convertToJadeTransactionOutput(): JTransactionOutput =
    JTransactionOutput(
        pubkey = publicKey.getStringFromKey(),
        hashId = hashId.base64Encode(),
        prevCoinbase = prevCoinbase.base64Encode(),
        payout = payout.toString(),
        tx = tx.map {
            it.base64Encode()
        }.toSet()
    )


fun Transaction.convertToJadeTransaction(): JTransaction =
    JTransaction(
        transactionId = hashId.base64Encode(),
        publicKey = publicKey.getStringFromKey(),
        data = data.convertToJadePhysicalData(),
        signature = signature.base64Encode(),
        ledgerId = chainId.convertToJadeChainId()
    )


fun PhysicalData.convertToJadePhysicalData(): JPhysicalData {
    val byteStream = ByteArrayOutputStream(approximateSize.toInt())
    ObjectOutputStream(byteStream).use {
        it.writeObject(data)
    }
    return JPhysicalData(
        data = byteStream.toByteArray().base64Encode(),
        seconds = instant.epochSecond,
        nanos = instant.nano,
        latitude = geoCoords?.latitude?.toString(),
        longitude = geoCoords?.longitude?.toString(),
        altitude = geoCoords?.altitude?.toString()
    )
}


//Conversions from Jade Types

fun JBlock.convertFromJadeBlock(
    builder: LedgerConfig.ByConfigBuilder
): Block =
    builder.block(
        transactions = data.asSequence().map {
            it.convertFromJadeTransaction(builder)
        }.toSortedSet(),
        coinbase = coinbase.convertFromJadeCoinbase(builder),
        blockHeader = header.convertFromJadeBlockHeader(builder),
        merkleTree = merkleTree.convertFromJadeMerkleTree(builder)
    )


fun JMerkleTree.convertFromJadeMerkleTree(
    builder: LedgerConfig.ByConfigBuilder
): MerkleTree =
    builder.merkletree(
        collapsedTree = hashes
            .asSequence()
            .map(String::base64DecodeToHash)
            .toMutableList(),
        levelIndex = levelIndex as MutableList<Int>
    )

fun JBlockHeader.convertFromJadeBlockHeader(
    builder: LedgerConfig.ByConfigBuilder
): BlockHeader =
    builder.blockheader(
        previousHash = previousHash.base64DecodeToHash(),
        hash = hash.base64DecodeToHash(),
        merkleRoot = merkleRoot.base64DecodeToHash(),
        seconds = seconds,
        nonce = nonce
    )

fun JLedgerId.convertFromJadeLedgerId(): LedgerId =
    LedgerId(
        id,
        hash.base64DecodeToHash()
    )


fun JCoinbase.convertFromJadeCoinbase(
    builder: LedgerConfig.ByConfigBuilder
): Coinbase =
    builder.coinbase(
        payoutTXO = payoutTXO
            .asSequence()
            .map(JTransactionOutput::convertFromJadeTransactionOutput)
            .toMutableSet(),
        payout = Payout(BigDecimal(payout)),
        hash = hashId.base64DecodeToHash(),
        difficulty = Difficulty(BigInteger(difficulty.base64Decode())),
        blockheight = blockheight
    )

private fun JTransactionOutput.convertFromJadeTransactionOutput(
): TransactionOutput =
    TransactionOutput(
        publicKey = pubkey.stringToPublicKey(),
        prevCoinbase = prevCoinbase.base64DecodeToHash(),
        hash = hashId.base64DecodeToHash(),
        payout = Payout(BigDecimal(payout)),
        tx = tx
            .asSequence()
            .map(String::base64DecodeToHash)
            .toMutableSet()
    )


fun JTransaction.convertFromJadeTransaction(
    builder: LedgerConfig.ByConfigBuilder
): Transaction =
    builder.transaction(
        publicKey.stringToPublicKey(),
        data.convertFromJadePhysicalData(),
        signature.base64Decode(),
        transactionId.base64DecodeToHash()
    )

fun JPhysicalData.convertFromJadePhysicalData(
): PhysicalData {
    val b = ByteArrayInputStream(data.base64Decode())
    val t = ObjectInputStream(b).use {
        it.readObject() as LedgerData
    }

    return when {
        latitude == null || longitude == null || altitude == null ->
            PhysicalData(
                Instant.ofEpochSecond(seconds, nanos.toLong()),
                null,
                t
            )
        else ->
            PhysicalData(
                Instant.ofEpochSecond(seconds, nanos.toLong()),
                GeoCoords(
                    BigDecimal(latitude),
                    BigDecimal(longitude),
                    BigDecimal(altitude)
                ),
                t
            )
    }
}



