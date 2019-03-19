package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import java.time.Instant

class BlockHeader(
    val ledgerId: Hash,
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty,
    val blockheight: Long,
    var hash: Hash,
    var merkleRoot: Hash,
    val previousHash: Hash,
    val params: BlockParams,
    var timestamp: Instant = Instant.now(),
    var nonce: Long = 0
) : Sizeable, Hashable, Storable, LedgerContract {


    override val approximateSize: Long =
        ClassLayout
            .parseClass(this::class.java)
            .instanceSize()


    constructor (
        blockChainId: Hash,
        previousHash: Hash,
        difficulty: Difficulty,
        blockheight: Long,
        blockParams: BlockParams
    ) : this(
        blockChainId,
        difficulty,
        blockheight,
        emptyHash(),
        emptyHash(),
        previousHash,
        blockParams
    )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("BlockHeader")
            .apply {
                setProperty("ledgerId", ledgerId)
                setProperty("difficulty", difficulty.toByteArray())
                setProperty("blockheight", blockheight)
                setProperty("hash", hash)
                setProperty("merkleRoot", merkleRoot)
                setProperty("previousHash", previousHash)
                setProperty("params", params)
                setProperty("seconds", timestamp.epochSecond)
                setProperty("nanos", timestamp.nano)
                setProperty("nonce", nonce)
            }


    /**
     * Hash is a cryptographic digest calculated from previous hash,
     * internalNonce, internalTimestamp, [MerkleTree]'s root
     * and each [Transaction]'s hash.
     */
    fun updateHash() {
        hash = digest(crypter)
    }


    override fun toString(): String = """
        |   Header: {
        |       BlockChainHash: ${ledgerId.print()}
        |       Difficulty: ${difficulty.print()}
        |       Blockheight: $blockheight
        |       PrevHash: ${previousHash.print()}
        |       Hash: ${hash.print()}
        |       MerkleRoot: ${merkleRoot.print()}
        |       Time: $timestamp
        |   }
        """.trimMargin()

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                ledgerId,
                difficulty.toByteArray(),
                blockheight.bytes(),
                previousHash,
                nonce.bytes(),
                timestamp.epochSecond.bytes(),
                timestamp.nano.bytes(),
                merkleRoot,
                params.blockLength.bytes(),
                params.blockMemSize.bytes()
            )
        )

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other !is BlockHeader)
            return false
        if (!ledgerId.contentEquals(
                other.ledgerId
            )
        )
            return false
        if (difficulty != other.difficulty)
            return false
        if (blockheight != other.blockheight)
            return false
        if (!hash.contentEquals(
                other.hash
            )
        )
            return false
        if (!merkleRoot.contentEquals(
                other.merkleRoot
            )
        )
            return false
        if (!previousHash.contentEquals(
                other.previousHash
            )
        )
            return false
        if (timestamp != other.timestamp)
            return false
        if (nonce != other.nonce)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = ledgerId.contentHashCode()
        result = 31 * result + difficulty.hashCode()
        result = 31 * result + blockheight.hashCode()
        result = 31 * result + hash.contentHashCode()
        result = 31 * result + merkleRoot.contentHashCode()
        result = 31 * result + previousHash.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}
