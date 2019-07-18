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
import org.knowledger.ledger.config.LedgerId
import org.knowledger.ledger.core.data.Difficulty
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.Payout
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.misc.base64Decode
import org.knowledger.ledger.core.misc.base64DecodeToHash
import org.knowledger.ledger.core.misc.base64Encode
import org.knowledger.ledger.core.misc.getStringFromKey
import org.knowledger.ledger.core.misc.stringToPublicKey
import org.knowledger.ledger.data.PhysicalData
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
import org.tinylog.Logger
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant


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
        merkleTree.nakedTree.map { it.base64Encode() },
        merkleTree.levelIndexes
    )

fun convertToJadeBlockHeader(header: BlockHeader): JBlockHeader =
    JBlockHeader(
        header.chainId,
        header.difficulty.toString(),
        header.blockheight,
        header.hashId.base64Encode(),
        header.merkleRoot.base64Encode(),
        header.previousHash.base64Encode(),
        header.params,
        header.timestamp.toString(),
        header.nonce
    )

fun convertToJadeBlockChainId(blid: LedgerId): JLedgerId =
    JLedgerId(
        blid.tag,
        blid.hashId.base64Encode()
    )


fun convertToJadeCoinbase(coinbase: Coinbase): JCoinbase =
    JCoinbase(
        null,
        coinbase.payoutTXO
            .map(::convertToJadeTransactionOutput)
            .toSet(),
        coinbase.payout.toString(),
        coinbase.hashId.base64Encode()
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
        coinbase.payout.toString(),
        coinbase.hashId.base64Encode()
    )

private fun convertToJadeTransactionOutput(txo: TransactionOutput): JTransactionOutput =
    JTransactionOutput(
        txo.publicKey.getStringFromKey(),
        txo.hashId.base64Encode(),
        txo.prevCoinbase.base64Encode(),
        txo.payout.toString(),
        txo.tx.map { it.base64Encode() }.toSet()
    )


fun convertToJadeTransaction(
    t: Transaction
): JTransaction =
    JTransaction(
        transactionId = t.hashId.base64Encode(),
        publicKey = t.publicKey.getStringFromKey(),
        data = convertToJadePhysicalData(
            t.data
        ),
        signature = t.signature.base64Encode()
    )


fun convertToJadeTransaction(
    blid: LedgerId,
    t: Transaction
): JTransaction =
    JTransaction(
        ledgerId = convertToJadeBlockChainId(blid),
        transactionId = t.hashId.base64Encode(),
        publicKey = t.publicKey.getStringFromKey(),
        data = convertToJadePhysicalData(t.data),
        signature = t.signature.base64Encode()
    )

fun convertToJadeTransaction(
    blid: Hash,
    t: Transaction
): JTransaction =
    JTransaction(
        blockChainHash = blid.base64Encode(),
        transactionId = t.hashId.base64Encode(),
        publicKey = t.publicKey.getStringFromKey(),
        data = convertToJadePhysicalData(t.data),
        signature = t.signature.base64Encode()
    )


fun convertToJadePhysicalData(data: PhysicalData): JPhysicalData {
    val byteStream = ByteArrayOutputStream(data.approximateSize.toInt())
    ObjectOutputStream(byteStream).use {
        it.writeObject(data.data)
    }
    return JPhysicalData(
        byteStream.toByteArray().base64Encode(),
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
        StorageUnawareBlock(
            b.data.map {
                convertFromJadeTransaction(hasher, it)
            }.toSortedSet(),
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
    StorageUnawareMerkleTree(
        hasher,
        merkleTree.hashes.map { it.base64DecodeToHash() },
        merkleTree.levelIndex
    )


fun convertFromJadeBlockHeader(
    hasher: Hasher,
    header: JBlockHeader
): BlockHeader =
    StorageUnawareBlockHeader(
        header.blid.base64DecodeToHash(),
        hasher,
        Difficulty(BigInteger(header.difficulty)),
        header.blockheight,
        header.hash.base64DecodeToHash(),
        header.merkleRoot.base64DecodeToHash(),
        header.previousHash.base64DecodeToHash(),
        header.params,
        Instant.parse(header.timeStamp),
        header.nonce
    )

fun convertFromJadeBlockChainId(blid: JLedgerId): LedgerId =
    LedgerId(
        blid.id,
        blid.hash.base64DecodeToHash()
    )


fun convertFromJadeCoinbase(
    coinbase: JCoinbase
): Coinbase =
    StorageUnawareCoinbase(
        coinbase.payoutTXO
            .map(::convertFromJadeTransactionOutput)
            .toMutableSet(),
        Payout(BigDecimal(coinbase.coinbase)),
        coinbase.hashId.base64DecodeToHash()
    )

private fun convertFromJadeTransactionOutput(
    txo: JTransactionOutput
): TransactionOutput =
    TransactionOutput(
        txo.pubkey.stringToPublicKey(),
        txo.prevCoinbase.base64DecodeToHash(),
        txo.hashId.base64DecodeToHash(),
        Payout(BigDecimal(txo.payout)),
        txo.tx.map {
            it.base64DecodeToHash()
        }.toMutableSet()
    )


fun convertFromJadeTransaction(
    hasher: Hasher,
    t: JTransaction
): Transaction =
    Transaction(
        t.publicKey.stringToPublicKey(),
        convertFromJadePhysicalData(t.data),
        base64Decode(t.signature),
        t.transactionId.base64DecodeToHash(),
        hasher
    )

fun convertFromJadePhysicalData(
    data: JPhysicalData
): PhysicalData {
    val b = ByteArrayInputStream(data.data.base64Decode())
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



