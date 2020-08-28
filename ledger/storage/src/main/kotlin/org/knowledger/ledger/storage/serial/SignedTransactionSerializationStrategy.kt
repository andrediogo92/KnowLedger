package org.knowledger.ledger.storage.serial

import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.crypto.serial.EncodedSignatureSerializer
import org.knowledger.ledger.storage.PhysicalData
import org.knowledger.ledger.storage.transaction.SignedTransaction

internal object SignedTransactionSerializationStrategy : SerializationStrategy<SignedTransaction> {
    private val encodedSignatureSerializer: SerializationStrategy<EncodedSignature>
        get() = EncodedSignatureSerializer
    private val publicKeySerializer: SerializationStrategy<EncodedPublicKey>
        get() = EncodedPublicKeySerializer
    private val physicalDataSerializer: SerializationStrategy<PhysicalData> =
        PhysicalData.serializer()

    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("Transaction") {
            element(elementName = "publicKey", descriptor = publicKeySerializer.descriptor)
            element(elementName = "signature", descriptor = encodedSignatureSerializer.descriptor)
            element(elementName = "data", descriptor = physicalDataSerializer.descriptor)
        }

    override fun serialize(encoder: Encoder, value: SignedTransaction) {
        compositeEncode(encoder) {
            encodeSerializableElement(descriptor, 0, publicKeySerializer, value.publicKey)
            encodeSerializableElement(descriptor, 1, encodedSignatureSerializer, value.signature)
            encodeSerializableElement(descriptor, 2, physicalDataSerializer, value.data)
        }
    }
}