package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.data.PhysicalData
import org.knowledger.ledger.serial.compositeEncode
import org.knowledger.ledger.storage.transaction.Transaction
import java.security.PublicKey

internal object TransactionSerializationStrategy : SerializationStrategy<Transaction> {
    val physicalDataSerializer: SerializationStrategy<PhysicalData> =
        PhysicalData.serializer()
    val publicKeySerializer: SerializationStrategy<PublicKey>
        get() = PublicKeySerializer


    override val descriptor: SerialDescriptor =
        SerialDescriptor("Transaction") {
            element(
                elementName = "publicKey",
                descriptor = publicKeySerializer.descriptor
            )
            element(
                elementName = "data",
                descriptor = physicalDataSerializer.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: Transaction) {
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0, publicKeySerializer, value.publicKey
            )
            encodeSerializableElement(
                descriptor, 1, physicalDataSerializer, value.data
            )
        }
    }
}