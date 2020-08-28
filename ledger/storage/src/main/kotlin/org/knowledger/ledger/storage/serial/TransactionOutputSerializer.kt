package org.knowledger.ledger.storage.serial

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.core.serial.compositeEncode
import org.knowledger.ledger.storage.Payout
import org.knowledger.ledger.storage.TransactionOutput

object TransactionOutputSerializer : KSerializer<TransactionOutput>, HashEncode {
    private val payoutSerializer: KSerializer<Payout> get() = PayoutSerializer

    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor =
        buildClassSerialDescriptor(serialName = "TransactionOutput") {
            val prevTxIndex = PrimitiveSerialDescriptor(
                serialName = "prevTxIndex", kind = PrimitiveKind.INT
            )
            val txIndex = PrimitiveSerialDescriptor(
                serialName = "txIndex", kind = PrimitiveKind.INT
            )
            element(elementName = "payout", descriptor = payoutSerializer.descriptor)
            element(elementName = "prevTxBlock", descriptor = hashDescriptor)
            element(elementName = prevTxIndex.serialName, descriptor = prevTxIndex)
            element(elementName = "prevTx", descriptor = hashDescriptor)
            element(elementName = txIndex.serialName, descriptor = txIndex)
            element(elementName = "tx", descriptor = hashDescriptor)
        }

    override fun deserialize(decoder: Decoder): Nothing {
        throw NotImplementedError()
    }

    override fun serialize(encoder: Encoder, value: TransactionOutput) {
        compositeEncode(encoder) {
            encodeSerializableElement(descriptor, 0, payoutSerializer, value.payout)
            encodeHash(1, value.prevTxBlock)
            encodeIntElement(descriptor, 2, value.prevTxIndex)
            encodeHash(3, value.prevTx)
            encodeIntElement(descriptor, 4, value.txIndex)
            encodeHash(5, value.tx)
        }
    }
}