package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.data.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import pt.um.lei.masb.blockchain.utils.generateSignature
import pt.um.lei.masb.blockchain.utils.getStringFromKey
import pt.um.lei.masb.blockchain.utils.verifyECDSASig
import java.security.PrivateKey
import java.security.PublicKey

class Transaction(
    // Agent's pub key.
    val publicKey: PublicKey,
    val data: PhysicalData,
    // This is to identify unequivocally an agent.
    val byteSignature: ByteArray
) : Sizeable, Hashed, Hashable, Storable {


    // This is also the hash of the transaction.
    override val hashId = digest(crypter)

    /**
     * Calculate the approximate size of the transaction.
     *
     * @return The size of the transaction in bytes.
     */
    override val approximateSize: Long
        get() = byteSize

    @Transient
    private var byteSize: Long = 0

    init {
        recalculateSize()
    }

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

    override fun store(): OElement {
        TODO("not implemented")
    }

    /**
     * Verifies the data we signed hasn't been tampered with.
     *
     * @return Whether the data was signed with the corresponding private key.
     */
    fun verifySignature(): Boolean =
        verifyECDSASig(
            publicKey,
            getStringFromKey(publicKey) +
                    data.digest(crypter),
            byteSignature
        )

    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(): Boolean {
        return verifySignature()
    }

    /**
     * Recalculates the Transaction size if it's necessary,
     *
     * The size of the transaction is lost when storing it
     * in database or serialization.
     */
    fun recalculateSize(): Long =
        let {
            byteSize =
                    ClassLayout
                        .parseClass(this::class.java)
                        .instanceSize() +
                    data.approximateSize
            byteSize
        }

    override fun toString(): String = """
            |       Transaction {
            |           Transaction id: $hashId},
            |           Public Key: $publicKey,
            |           $data
            |       }
            """.trimMargin()

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            ${getStringFromKey(publicKey)}
            ${data.digest(c)}
            $byteSignature
            """.trimIndent()
        )

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }

}
