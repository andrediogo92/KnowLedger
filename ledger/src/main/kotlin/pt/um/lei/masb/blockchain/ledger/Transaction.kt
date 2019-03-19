package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.service.Ident
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import pt.um.lei.masb.blockchain.utils.asHex
import pt.um.lei.masb.blockchain.utils.flattenBytes
import pt.um.lei.masb.blockchain.utils.generateSignature
import pt.um.lei.masb.blockchain.utils.verifyECDSASig
import java.security.PrivateKey
import java.security.PublicKey

data class Transaction(
    // Agent's pub key.
    val publicKey: PublicKey,
    val data: PhysicalData,
    // This is to identify unequivocally an agent.
    val signature: ByteArray
) : Sizeable,
    Hashed,
    Hashable,
    Storable,
    LedgerContract {


    // This is also the hash of the transaction.
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
        ident: Ident,
        data: PhysicalData
    ) : this(
        ident.publicKey,
        data,
        generateSignature(
            ident.privateKey,
            ident.publicKey,
            data
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
            data
        )
    )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Transaction")
            .apply {
                setProperty("publicKey", publicKey.encoded)
                setProperty("data", data.store(session))
                setProperty(
                    "signature",
                    session.newInstance(
                        signature
                    )
                )
                setProperty("hashId", hashId)
            }

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
            publicKey.encoded + data.digest(crypter),
            signature
        )

    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(): Boolean {
        return verifySignature()
    }


    override fun toString(): String = """
            |       Transaction {
            |           Transaction id: ${hashId.print()},
            |           Public Key: ${publicKey.encoded.asHex()},
            |$data,
            |           Signature: ${signature.asHex()}
            |       }
            """.trimMargin()

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                publicKey.encoded,
                data.digest(c),
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
        val crypter = DEFAULT_CRYPTER
    }

}
