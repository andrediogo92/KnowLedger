package org.knowledger.ledger.storage.transaction

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.storage.PhysicalData

internal data class SignedTransactionImpl(
    override val publicKey: EncodedPublicKey,
    override val data: PhysicalData,
    // This is to identify unequivocally an agent.
    override val signature: EncodedSignature,
) : SignedTransaction {
    @OptIn(ExperimentalSerializationApi::class)
    override fun processTransaction(encoder: BinaryFormat): Boolean {
        return verifySignature(encoder)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SignedTransaction) return false

        if (publicKey != other.publicKey) return false
        if (data != other.data) return false
        if (signature != other.signature) return false

        return true
    }

    override fun hashCode(): Int {
        var result = publicKey.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + signature.hashCode()
        return result
    }
}