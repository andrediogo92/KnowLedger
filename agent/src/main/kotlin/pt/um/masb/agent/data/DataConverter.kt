package pt.um.masb.agent.data

import kotlinx.io.ByteArrayInputStream
import kotlinx.io.ByteArrayOutputStream
import org.tinylog.Logger
import pt.um.masb.agent.messaging.block.ontology.concepts.JBlock
import pt.um.masb.agent.messaging.block.ontology.concepts.JBlockHeader
import pt.um.masb.agent.messaging.block.ontology.concepts.JCoinbase
import pt.um.masb.agent.messaging.block.ontology.concepts.JLedgerId
import pt.um.masb.agent.messaging.block.ontology.concepts.JMerkleTree
import pt.um.masb.agent.messaging.block.ontology.concepts.JTransactionOutput
import pt.um.masb.agent.messaging.transaction.ontology.concepts.JPhysicalData
import pt.um.masb.agent.messaging.transaction.ontology.concepts.JTransaction
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.Difficulty
import pt.um.masb.common.data.Payout
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.base64Decode
import pt.um.masb.common.misc.base64DecodeToHash
import pt.um.masb.common.misc.base64Encode
import pt.um.masb.common.misc.getStringFromKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.data.MerkleTree
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.storage.Block
import pt.um.masb.ledger.storage.BlockHeader
import pt.um.masb.ledger.storage.Coinbase
import pt.um.masb.ledger.storage.Transaction
import pt.um.masb.ledger.storage.TransactionOutput
import java.io.InvalidClassException
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.*


fun convertToJadeBlock(
    b: Block,
    clazz: Class<out BlockChainData>
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
        base64Encode(header.ledgerId),
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
        blid.uuid.toString(),
        blid.timestamp.toString(),
        blid.params.copy(),
        blid.id,
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
        getStringFromKey(txo.publicKey),
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
        publicKey = getStringFromKey(t.publicKey),
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
        publicKey = getStringFromKey(t.publicKey),
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
        publicKey = getStringFromKey(t.publicKey),
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
    clazz: Class<out BlockChainData>
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
        it.readObject() as BlockChainData
    }

    return PhysicalData(
        Instant.parse(data.instant),
        BigDecimal(data.lat),
        BigDecimal(data.lng),
        t
    )
}



