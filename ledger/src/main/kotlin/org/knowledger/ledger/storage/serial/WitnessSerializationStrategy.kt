package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Encoder
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationStrategy
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashEncode
import org.knowledger.ledger.serial.SortedListSerializer
import org.knowledger.ledger.serial.compositeEncode
import org.knowledger.ledger.storage.witness.Witness

internal object WitnessSerializationStrategy : SerializationStrategy<Witness>,
                                               HashEncode {
    val encodedPublicKeySerializer: SerializationStrategy<EncodedPublicKey>
        get() = EncodedPublicKeySerializer
    val payoutSerializer: SerializationStrategy<Payout>
        get() = PayoutSerializer
    private val transactionOutputsSerializer =
        SortedListSerializer(TransactionOutputSerializer)


    override val descriptor: SerialDescriptor =
        SerialDescriptor("TransactionOutput") {
            val previousWitnessIndex = PrimitiveDescriptor(
                "previousWitnessIndex", PrimitiveKind.INT
            )
            element(
                elementName = "publicKey",
                descriptor = encodedPublicKeySerializer.descriptor
            )
            element(
                elementName = previousWitnessIndex.serialName,
                descriptor = previousWitnessIndex
            )
            element(
                elementName = "previousCoinbase",
                descriptor = hashDescriptor
            )
            element(
                elementName = "payout",
                descriptor = payoutSerializer.descriptor
            )
            element(
                elementName = "transactionOutputs",
                descriptor = transactionOutputsSerializer.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: Witness): Unit =
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0,
                encodedPublicKeySerializer, value.publicKey
            )
            encodeIntElement(descriptor, 1, value.previousWitnessIndex)
            encodeHash(2, value.previousCoinbase)
            encodeSerializableElement(
                descriptor, 3, payoutSerializer, value.payout
            )
            encodeSerializableElement(
                descriptor, 4, transactionOutputsSerializer,
                value.transactionOutputs
            )
        }
}