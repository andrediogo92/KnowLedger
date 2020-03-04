@file:UseSerializers(EncodedSignatureByteSerializer::class)

package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.generateSignature
import org.knowledger.ledger.core.verifyECDSASig
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.serial.EncodedSignatureByteSerializer
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.service.Identity
import java.security.PrivateKey
import java.security.PublicKey

@Serializable
internal data class SignedTransactionImpl(
    val transaction: TransactionImpl,
    // This is to identify unequivocally an agent.
    internal var _signature: EncodedSignature
) : SignedTransaction,
    Transaction by transaction {
    override val signature: EncodedSignature
        get() = _signature

    internal constructor(
        transaction: TransactionImpl,
        privateKey: PrivateKey, encoder: BinaryFormat
    ) : this(
        transaction = transaction,
        _signature = EncodedSignature(ByteArray(0))
    ) {
        _signature = privateKey.generateSignature(
            transaction, encoder
        )
    }


    internal constructor(
        identity: Identity, data: PhysicalData,
        encoder: BinaryFormat
    ) : this(
        transaction = TransactionImpl(
            publicKey = identity.publicKey,
            data = data
        ), privateKey = identity.privateKey,
        encoder = encoder
    )

    internal constructor(
        privateKey: PrivateKey, publicKey: PublicKey,
        data: PhysicalData, encoder: BinaryFormat
    ) : this(
        transaction = TransactionImpl(
            publicKey = publicKey,
            data = data
        ), privateKey = privateKey,
        encoder = encoder
    )

    internal constructor(
        publicKey: PublicKey, data: PhysicalData,
        signature: ByteArray
    ) : this(
        publicKey, data,
        EncodedSignature(signature)
    )

    internal constructor(
        publicKey: PublicKey,
        data: PhysicalData,
        signature: EncodedSignature
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

    override fun clone(): SignedTransactionImpl =
        copy(
            transaction = transaction.clone(),
            _signature = _signature
        )

    override fun verifySignature(encoder: BinaryFormat): Boolean {
        return signature.verifyECDSASig(
            transaction.publicKey,
            transaction.serialize(encoder)
        )
    }

    override fun processTransaction(encoder: BinaryFormat): Boolean {
        return verifySignature(encoder)
    }


    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignedTransaction) return false

        if (transaction != other) return false
        if (signature != other.signature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = transaction.hashCode()
        result = 31 * result + signature.hashCode()
        return result
    }
}