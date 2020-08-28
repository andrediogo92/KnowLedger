package org.knowledger.ledger.storage.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.witness.Witness

internal object WitnessSerializationStrategy : SerializationStrategy<Witness>, HashEncode {
    private val encodedPublicKeySerializer: SerializationStrategy<EncodedPublicKey>
        get() = EncodedPublicKeySerializer
    private val payoutSerializer: SerializationStrategy<Payout> get() = PayoutSerializer
    private val transactionOutputsSerializer = SortedListSerializer(TransactionOutputSerializer)

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor("TransactionOutput") {
            val previousWitnessIndex = PrimitiveSerialDescriptor(
                "previousWitnessIndex", PrimitiveKind.INT
            )
            element(elementName = "publicKey", descriptor = encodedPublicKeySerializer.descriptor)
            element(
                elementName = previousWitnessIndex.serialName, descriptor = previousWitnessIndex
            )
            element(elementName = "previousCoinbase", descriptor = hashDescriptor)
            element(elementName = "payout", descriptor = payoutSerializer.descriptor)
            element(
                elementName = "transactionOutputs",
                descriptor = transactionOutputsSerializer.descriptor
            )
        }

    override fun serialize(encoder: Encoder, value: Witness): Unit =
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0, encodedPublicKeySerializer, value.publicKey
            )
            encodeIntElement(descriptor, 1, value.previousWitnessIndex)
            encodeHash(2, value.previousCoinbase)
            encodeSerializableElement(descriptor, 3, payoutSerializer, value.payout)
            encodeSerializableElement(
                descriptor, 4, transactionOutputsSerializer, value.transactionOutputs
            )
        }
}