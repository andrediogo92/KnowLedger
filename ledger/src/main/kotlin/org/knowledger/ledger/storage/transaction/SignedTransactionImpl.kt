@file:UseSerializers(ByteArraySerializer::class)

package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.misc.generateSignature
import org.knowledger.ledger.core.misc.verifyECDSASig
import org.knowledger.ledger.core.serial.ByteArraySerializer
import org.knowledger.ledger.crypto.service.Identity
import java.security.PrivateKey
import java.security.PublicKey

@Serializable
@SerialName("SignedTransaction")
internal data class SignedTransactionImpl(
    val transaction: TransactionImpl,
    // This is to identify unequivocally an agent.
    @SerialName("signature")
    internal var _signature: ByteArray
) : SignedTransaction,

    Transaction by transaction {
    override val signature: ByteArray
        get() = _signature

    constructor(
        transaction: TransactionImpl,
        privateKey: PrivateKey, cbor: Cbor
    ) : this(transaction, ByteArray(0)) {
        _signature = generateSignature(
            privateKey, transaction,
            cbor
        )
    }


    constructor(
        identity: Identity, data: PhysicalData,
        cbor: Cbor
    ) : this(
        TransactionImpl(identity.publicKey, data),
        identity.privateKey, cbor
    )

    constructor(
        privateKey: PrivateKey,
        publicKey: PublicKey, data: PhysicalData,
        cbor: Cbor
    ) : this(
        TransactionImpl(publicKey, data),
        privateKey, cbor
    )

    constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray
    ) : this(
        TransactionImpl(publicKey, data),
        signature
    )

    override fun serialize(
        cbor: Cbor
    ): ByteArray =
        cbor.dump(serializer(), this)

    override fun compareTo(
        other: Transaction
    ): Int =
        transaction.compareTo(other)


    override fun verifySignature(cbor: Cbor): Boolean {
        return verifyECDSASig(
            transaction.publicKey,
            transaction.serialize(cbor),
            signature
        )
    }

    override fun processTransaction(cbor: Cbor): Boolean {
        return verifySignature(cbor)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignedTransactionImpl) return false

        if (transaction != other.transaction) return false
        if (!signature.contentEquals(other.signature)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transaction.hashCode()
        result = 31 * result + signature.contentHashCode()
        return result
    }
}