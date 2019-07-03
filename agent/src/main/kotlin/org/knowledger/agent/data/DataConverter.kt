package org.knowledger.agent.data

import com.sun.xml.internal.messaging.saaj.util.Base64.base64Decode
import org.knowledger.agent.messaging.block.ontology.concepts.JBlock
import org.knowledger.agent.messaging.block.ontology.concepts.JBlockHeader
import org.knowledger.agent.messaging.block.ontology.concepts.JCoinbase
import org.knowledger.agent.messaging.block.ontology.concepts.JLedgerId
import org.knowledger.agent.messaging.block.ontology.concepts.JMerkleTree
import org.knowledger.agent.messaging.block.ontology.concepts.JTransactionOutput
import org.knowledger.agent.messaging.transaction.ontology.concepts.JPhysicalData
import org.knowledger.agent.messaging.transaction.ontology.concepts.JTransaction
import org.knowledger.common.data.Difficulty
import org.knowledger.common.data.LedgerData
import org.knowledger.common.data.Payout
import org.knowledger.common.hash.Hash
import org.knowledger.common.hash.Hasher
import org.knowledger.common.misc.base64Decode
import org.knowledger.common.misc.base64DecodeToHash
import org.knowledger.common.misc.base64Encode
import org.knowledger.common.misc.getStringFromKey
import org.knowledger.common.misc.stringToPublicKey
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.data.MerkleTree
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.storage.TransactionOutput
import org.tinylog.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.*


fun convertToJadeBlock(
    b: Block,
    clazz: Class<out LedgerData>
): JBlock =
    JBlock(
        b.data
            .map { convertToJadeTransaction(it) }
            .toList(),
        convertToJadeCoinbase(b.coinbase),
        convertToJadeBlockHeader(b.header),
        convertToJadeMerkleTree(b.merkleTree),
        clazz.simpleName
    )

fun convertToJadeMerkleTree(merkleTree: MerkleTree): JMerkleTree =
    JMerkleTree(
        merkleTree.nakedTree.map { base64Encode(it) },
        merkleTree.levelIndexes
    )

fun convertToJadeBlockHeader(header: BlockHeader): JBlockHeader =
    JBlockHeader(
        base64Encode(header.chainId),
        header.difficulty.toString(),
        header.blockheight,
        base64Encode(header.hashId),
        base64Encode(header.merkleRoot),
        base64Encode(header.previousHash),
        header.params,
        header.timestamp.toString(),
        header.nonce
    )

fun convertToJadeBlockChainId(blid: LedgerId): JLedgerId =
    JLedgerId(
        blid.tag,
        base64Encode(blid.hashId)
    )


fun convertToJadeCoinbase(coinbase: Coinbase): JCoinbase =
    JCoinbase(
        null,
        coinbase.payoutTXO
            .map(::convertToJadeTransactionOutput)
            .toSet(),
        coinbase.coinbase.toString(),
        base64Encode(coinbase.hashId)
    )

fun convertToJadeCoinbase(
    blid: LedgerId,
    coinbase: Coinbase
): JCoinbase =
    JCoinbase(
        convertToJadeBlockChainId(blid),
        coinbase.payoutTXO
            .map(::convertToJadeTransactionOutput)
            .toSet(),
        coinbase.coinbase.toString(),
        base64Encode(coinbase.hashId)
    )

private fun convertToJadeTransactionOutput(txo: TransactionOutput): JTransactionOutput =
    JTransactionOutput(
        txo.publicKey.getStringFromKey(),
        base64Encode(txo.hashId),
        base64Encode(txo.prevCoinbase),
        txo.payout.toString(),
        txo.tx.map { base64Encode(it) }.toSet()
    )


fun convertToJadeTransaction(
    t: Transaction
): JTransaction =
    JTransaction(
        transactionId = base64Encode(t.hashId),
        publicKey = t.publicKey.getStringFromKey(),
        data = convertToJadePhysicalData(
            t.data
        ),
        signature = base64Encode(t.signature)
    )


fun convertToJadeTransaction(
    blid: LedgerId,
    t: Transaction
): JTransaction =
    JTransaction(
        ledgerId = convertToJadeBlockChainId(blid),
        transactionId = base64Encode(t.hashId),
        publicKey = t.publicKey.getStringFromKey(),
        data = convertToJadePhysicalData(t.data),
        signature = base64Encode(t.signature)
    )

fun convertToJadeTransaction(
    blid: Hash,
    t: Transaction
): JTransaction =
    JTransaction(
        blockChainHash = base64Encode(blid),
        transactionId = base64Encode(t.hashId),
        publicKey = t.publicKey.getStringFromKey(),
        data = convertToJadePhysicalData(t.data),
        signature = base64Encode(t.signature)
    )


fun convertToJadePhysicalData(data: PhysicalData): JPhysicalData {
    val byteStream = ByteArrayOutputStream(data.approximateSize.toInt())
    ObjectOutputStream(byteStream).use {
        it.writeObject(data.data)
    }
    return JPhysicalData(
        base64Encode(byteStream.toByteArray()),
        data.instant.toString(),
        data.geoCoords?.latitude.toString(),
        data.geoCoords?.longitude.toString()
    )
}


//Conversions from Jade Types

fun convertFromJadeBlock(
    b: JBlock,
    hasher: Hasher,
    clazz: Class<out LedgerData>
): Block =
    if (clazz.simpleName == b.clazz) {
        Block(
            b.data.map {
                convertFromJadeTransaction(hasher, it)
            }.toMutableList(),
            convertFromJadeCoinbase(b.coinbase),
            convertFromJadeBlockHeader(hasher, b.header),
            convertFromJadeMerkleTree(hasher, b.merkleTree)
        )
    } else {
        val err = "Incompatible types on JBlock of type ${b.clazz} and Block of type ${clazz.simpleName}"
        Logger.error { err }
        throw InvalidClassException(err)
    }

fun convertFromJadeMerkleTree(
    hasher: Hasher,
    merkleTree: JMerkleTree
): MerkleTree =
    MerkleTree(
        hasher,
        merkleTree.hashes.map { base64DecodeToHash(it) },
        merkleTree.levelIndex
    )


fun convertFromJadeBlockHeader(
    hasher: Hasher,
    header: JBlockHeader
): BlockHeader =
    BlockHeader(
        base64DecodeToHash(header.blid),
        hasher,
        Difficulty(BigInteger(header.difficulty)),
        header.blockheight,
        base64DecodeToHash(header.hash),
        base64DecodeToHash(header.merkleRoot),
        base64DecodeToHash(header.previousHash),
        header.params,
        Instant.parse(header.timeStamp),
        header.nonce
    )

fun convertFromJadeBlockChainId(blid: JLedgerId): LedgerId =
    LedgerId(
        blid.id,
        UUID.fromString(blid.uuid),
        Instant.parse(blid.timestamp),
        blid.params,
        base64DecodeToHash(blid.hash)
    )


fun convertFromJadeCoinbase(
    coinbase: JCoinbase
): Coinbase =
    Coinbase(
        coinbase.payoutTXO
            .map(::convertFromJadeTransactionOutput)
            .toMutableSet(),
        Payout(BigDecimal(coinbase.coinbase)),
        base64DecodeToHash(coinbase.hashId)
    )

private fun convertFromJadeTransactionOutput(
    txo: JTransactionOutput
): TransactionOutput =
    TransactionOutput(
        stringToPublicKey(txo.pubkey),
        base64DecodeToHash(txo.prevCoinbase),
        base64DecodeToHash(txo.hashId),
        Payout(BigDecimal(txo.payout)),
        txo.tx.map {
            base64DecodeToHash(it)
        }.toMutableSet()
    )


fun convertFromJadeTransaction(
    hasher: Hasher,
    t: JTransaction
): Transaction =
    Transaction(
        stringToPublicKey(t.publicKey),
        convertFromJadePhysicalData(t.data),
        base64Decode(t.signature),
        base64DecodeToHash(t.transactionId),
        hasher
    )

fun convertFromJadePhysicalData(
    data: JPhysicalData
): PhysicalData {
    val b = ByteArrayInputStream(base64Decode(data.data))
    val t = ObjectInputStream(b).use {
        it.readObject() as LedgerData
    }

    return PhysicalData(
        Instant.parse(data.instant),
        BigDecimal(data.lat),
        BigDecimal(data.lng),
        t
    )
}



