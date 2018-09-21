package pt.um.lei.masb.blockchain

import mu.KLogging
import org.openjdk.jol.info.ClassLayout
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.PhysicalData
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.DigestAble
import pt.um.lei.masb.blockchain.utils.generateSignature
import pt.um.lei.masb.blockchain.utils.getStringFromKey
import pt.um.lei.masb.blockchain.utils.verifyECDSASig
import java.security.PrivateKey
import java.security.PublicKey
import java.util.concurrent.atomic.AtomicLong

class Transaction<T : BlockChainData<T>>(
        val blockChainId: BlockChainId,
        // Agent's pub key.
        val publicKey: PublicKey,
        val data: PhysicalData<T>,
        // This is to identify unequivocally an agent.
        val byteSignature: ByteArray) : Sizeable, Hashed, DigestAble {

    companion object : KLogging() {
        // A rough count of how many transactions have been generated.
        private val SEQUENCE: AtomicLong = AtomicLong(0)
    }


    // This is also the hash of the transaction.
    override val hashId = calculateHash()

    /**
     * Calculate the approximate size of the transaction.
     *
     * @return The size of the transaction in bytes.
     */
    override val approximateSize: Long
        get() = byteSize




    @Transient
    private var byteSize =
            ClassLayout.parseClass(this::class.java).instanceSize() +
            data.approximateSize

    constructor(blockChainId: BlockChainId,
                ident: Ident,
                data: PhysicalData<T>) : this(blockChainId,
                                              ident.publicKey,
                                              data,
                                              generateSignature(ident.privateKey,
                                                                ident.publicKey,
                                                                data))


    constructor(blockChainId: BlockChainId,
                privateKey: PrivateKey,
                publicKey: PublicKey,
                data: PhysicalData<T>) : this(blockChainId,
                                              publicKey,
                                              data,
                                              generateSignature(privateKey,
                                                                publicKey,
                                                                data))


    /**
     *  This Calculates the transaction hash (which will be used as its Id).
     *  @return Hash generated from public key, data and sequence.
     */
    private fun calculateHash(): String =
        //Increase the sequence to avoid 2 identical transactions having the same hash
            DEFAULT_CRYPTER.applyHash(getStringFromKey(publicKey) +
                                      //Marshall this first.
                                      data.digest(DEFAULT_CRYPTER))


    /**
     * Verifies the data we signed hasn't been tampered with.
     *
     * @return Whether the data was signed with the corresponding private key.
     */
    fun verifySignature(): Boolean =
            verifyECDSASig(publicKey,
                           getStringFromKey(publicKey) +
                           data.digest(DEFAULT_CRYPTER),
                           byteSignature)


    /**
     * TODO: Transaction verification.
     * @return Whether the transaction is valid.
     */
    fun processTransaction(): Boolean {
        return true//verifySignature();
    }


    /**
     * Recalculates the Transaction size if it's necessary,
     *
     * The size of the transaction is lost when storing it in database or serialization.
     */
    fun resetSize() {
        byteSize = ClassLayout.parseClass(this::class.java).instanceSize() +
                data.approximateSize
    }

    override fun toString(): String =
            "Transaction {${System.lineSeparator()}Transaction id: $hashId${System.lineSeparator()}Public Key: $publicKey${System.lineSeparator()}Data : {$data}${System.lineSeparator()}"


    override fun digest(c: Crypter): String =
            c.applyHash("${getStringFromKey(publicKey)}${data.digest(c)}$byteSignature")
}
