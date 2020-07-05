package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.serial.EncodedSignatureSerializer
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.compositeEncode
import org.knowledger.ledger.storage.transaction.SignedTransaction
import java.security.PublicKey

internal object SignedTransactionSerializationStrategy : SerializationStrategy<SignedTransaction> {
    val encodedSignatureSerializer: SerializationStrategy<EncodedSignature> =
        EncodedSignatureSerializer
    val publicKeySerializer: SerializationStrategy<PublicKey> =
        PublicKeySerializer
    val physicalDataSerializer: SerializationStrategy<PhysicalData> =
        PhysicalData.serializer()

    override val descriptor: SerialDescriptor =
        SerialDescriptor("Transaction") {
            element(
                elementName = "publicKey",
                descriptor = publicKeySerializer.descriptor
            )
            element(
                elementName = "signature",
                descriptor = encodedSignatureSerializer.descriptor
            )
            element(
                elementName = "data",
                descriptor = physicalDataSerializer.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: SignedTransaction) {
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0, publicKeySerializer, value.publicKey
            )
            encodeSerializableElement(
                descriptor, 1, encodedSignatureSerializer, value.signature
            )
            encodeSerializableElement(
                descriptor, 2, physicalDataSerializer, value.data
            )
        }
    }
}