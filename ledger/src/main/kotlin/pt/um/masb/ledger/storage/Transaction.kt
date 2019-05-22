package pt.um.masb.ledger.storage

import com.squareup.moshi.JsonClass
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.masb.common.Sizeable
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.misc.generateSignature
import pt.um.masb.common.misc.verifyECDSASig
import pt.um.masb.common.storage.LedgerContract
import pt.um.masb.ledger.data.PhysicalData
import pt.um.masb.ledger.service.Identity
import java.security.PrivateKey
import java.security.PublicKey

@JsonClass(generateAdapter = true)
data class Transaction(
    // Agent's pub key.
    val publicKey: PublicKey,
    val data: PhysicalData,
    // This is to identify unequivocally an agent.
    val signature: ByteArray
) : Sizeable,
    Hashed,
    Hashable,
    LedgerContract {


    // This is also the hashId of the transaction.
    override val hashId = digest(crypter)

    /**
     * Calculate the approximate size of the transaction.
     *
     * @return The size of the transaction in bytes.
     */
    override val approximateSize: Long
        get() = ClassLayout
            .parseClass(this::class.java)
            .instanceSize() + data.approximateSize


    constructor(
        identity: Identity,
        data: PhysicalData
    ) : this(
        identity.publicKey,
        data,
        generateSignature(
            identity.privateKey,
            identity.publicKey,
            data,
            crypter
        )
    )

    constructor(
        privateKey: PrivateKey,
        publicKey: PublicKey,
        data: PhysicalData
    ) : this(
        publicKey,
        data,
        generateSignature(
            privateKey,
            publicKey,
            data,
            crypter
        )
    )

    /**
     * Verifies the data we signed hasn't been
     * tampered with.
     *
     * @return Whether the data was signed with the
     * corresponding private key.
     */
    fun verifySignature(): Boolean =
        verifyECDSASig(
            publicKey,
            publicKey.encoded + data.digest(crypter).bytes,
            signature
        )

    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(): Boolean {
        return verifySignature()
    }


    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                publicKey.encoded,
                data.digest(c).bytes,
                signature
            )
        )


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Transaction) return false

        if (!publicKey.encoded!!.contentEquals(
                other.publicKey.encoded
            )
        ) return false
        if (data != other.data) return false
        if (!signature.contentEquals(
                other.signature
            )
        ) return false
        if (!hashId.contentEquals(
                other.hashId
            )
        ) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + signature.contentHashCode()
        result = 31 * result + hashId.contentHashCode()
        return result
    }

    companion object : KLogging() {
        val crypter = AvailableHashAlgorithms.SHA256Hasher
    }

}
