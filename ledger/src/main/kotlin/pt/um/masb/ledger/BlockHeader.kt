package pt.um.masb.ledger

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Difficulty
import pt.um.masb.common.Hash
import pt.um.masb.common.Hashable
import pt.um.masb.common.Hashed
import pt.um.masb.common.Sizeable
import pt.um.masb.common.crypt.AvailableCrypters
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.emptyHash
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.config.BlockParams
import java.time.Instant

@JsonClass(generateAdapter = true)
data class BlockHeader(
    val ledgerId: Hash,
    // Difficulty is fixed at block generation time.
    val difficulty: Difficulty,
    val blockheight: Long,
    override var hashId: Hash,
    var merkleRoot: Hash,
    val previousHash: Hash,
    val params: BlockParams,
    var timestamp: Instant = Instant.now(),
    var nonce: Long = 0
) : Sizeable, Hashed, Hashable, Storable,
    LedgerContract {

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
                setProperty("ledgerHash", ledgerId)
                setProperty("difficulty", difficulty.toByteArray())
                setProperty("blockheight", blockheight)
                setProperty("hashId", hashId)
                setProperty("merkleRoot", merkleRoot)
                setProperty("previousHash", previousHash)
                setProperty("params", params)
                setProperty("seconds", timestamp.epochSecond)
                setProperty("nanos", timestamp.nano)
                setProperty("nonce", nonce)
            }


    /**
     * Hash is a cryptographic digest calculated from previous hashId,
     * internalNonce, internalTimestamp, [MerkleTree]'s root
     * and each [Transaction]'s hashId.
     */
    fun updateHash() {
        hashId = digest(crypter)
    }


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
        if (!hashId.contentEquals(
                other.hashId
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
        result = 31 * result + hashId.contentHashCode()
        result = 31 * result + merkleRoot.contentHashCode()
        result = 31 * result + previousHash.contentHashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + nonce.hashCode()
        return result
    }

    companion object : KLogging() {
        val crypter = AvailableCrypters.SHA256Encrypter
    }
}
