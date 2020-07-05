package org.knowledger.ledger.storage.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.HashEncode
import org.knowledger.ledger.serial.compositeEncode
import org.knowledger.ledger.storage.TransactionOutput

@Serializer(forClass = TransactionOutput::class)
object TransactionOutputSerializer : KSerializer<TransactionOutput>,
                                     HashEncode {
    val payoutSerializer: KSerializer<Payout>
        get() = PayoutSerializer

    override val descriptor: SerialDescriptor =
        SerialDescriptor(serialName = "TransactionOutput") {
            val prevTxIndex = PrimitiveDescriptor(
                serialName = "prevTxIndex", kind = PrimitiveKind.INT
            )
            val txIndex = PrimitiveDescriptor(
                serialName = "txIndex", kind = PrimitiveKind.INT
            )
            element(
                elementName = "payout", descriptor = payoutSerializer.descriptor
            )
            element(
                elementName = "prevTxBlock", descriptor = hashDescriptor
            )
            element(
                elementName = prevTxIndex.serialName, descriptor = prevTxIndex
            )
            element(
                elementName = "prevTx", descriptor = hashDescriptor
            )
            element(
                elementName = txIndex.serialName, descriptor = txIndex
            )
            element(
                elementName = "tx", descriptor = hashDescriptor
            )
        }

    override fun deserialize(decoder: Decoder): Nothing {
        throw NotImplementedError()
    }

    override fun serialize(encoder: Encoder, value: TransactionOutput) {
        compositeEncode(encoder) {
            encodeSerializableElement(
                descriptor, 0, payoutSerializer, value.payout
            )
            encodeHash(1, value.prevTxBlock)
            encodeIntElement(descriptor, 2, value.prevTxIndex)
            encodeHash(3, value.prevTx)
            encodeIntElement(descriptor, 4, value.txIndex)
            encodeHash(5, value.tx)
        }
    }
}