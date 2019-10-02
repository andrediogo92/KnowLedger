@file:UseSerializers(ByteArraySerializer::class)

package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.internal.ByteArraySerializer
import org.knowledger.ledger.core.data.PhysicalData
import org.knowledger.ledger.core.misc.generateSignature
import org.knowledger.ledger.core.misc.verifyECDSASig
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
        privateKey: PrivateKey, encoder: BinaryFormat
    ) : this(
        transaction = transaction,
        _signature = ByteArray(0)
    ) {
        _signature = generateSignature(
            privateKey, transaction,
            encoder
        )
    }


    constructor(
        identity: Identity, data: PhysicalData,
        encoder: BinaryFormat
    ) : this(
        transaction = TransactionImpl(
            publicKey = identity.publicKey,
            data = data
        ), privateKey = identity.privateKey,
        encoder = encoder
    )

    constructor(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, encoder: BinaryFormat
    ) : this(
        transaction = TransactionImpl(
            publicKey = publicKey,
            data = data
        ), privateKey = privateKey,
        encoder = encoder
    )

    constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray
    ) : this(
        transaction = TransactionImpl(
            publicKey = publicKey,
            data = data
        ), _signature = signature
    )

    override fun serialize(
        encoder: BinaryFormat
    ): ByteArray =
        encoder.dump(serializer(), this)

    override fun compareTo(
        other: Transaction
    ): Int =
        transaction.compareTo(other)


    override fun verifySignature(encoder: BinaryFormat): Boolean {
        return verifyECDSASig(
            transaction.publicKey,
            transaction.serialize(encoder),
            signature
        )
    }

    override fun processTransaction(encoder: BinaryFormat): Boolean {
        return verifySignature(encoder)
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