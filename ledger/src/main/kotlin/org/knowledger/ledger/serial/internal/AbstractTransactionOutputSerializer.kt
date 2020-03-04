package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.transactionOutput
import kotlin.properties.Delegates

internal abstract class AbstractTransactionOutputSerializer : KSerializer<TransactionOutput>,
                                                              HashEncode {
    override val descriptor: SerialDescriptor =
        SerialDescriptor("TransactionOutput") {
            val prevTxIndex = PrimitiveDescriptor(
                "prevTxIndex", PrimitiveKind.INT
            )
            val txIndex = PrimitiveDescriptor(
                "txIndex", PrimitiveKind.INT
            )
            element(
                elementName = "payout",
                descriptor = PayoutSerializer.descriptor
            )
            element(
                elementName = "prevTxBlock",
                descriptor = hashDescriptor
            )
            element(
                elementName = prevTxIndex.serialName,
                descriptor = prevTxIndex
            )
            element(
                elementName = "prevTx",
                descriptor = hashDescriptor
            )
            element(
                elementName = txIndex.serialName,
                descriptor = txIndex
            )
            element(
                elementName = "tx",
                descriptor = hashDescriptor
            )
        }


    override fun deserialize(decoder: Decoder): TransactionOutput =
        with(decoder.beginStructure(descriptor)) {
            lateinit var payout: Payout
            lateinit var prevTxBlock: Hash
            var prevTxIndex by Delegates.notNull<Int>()
            lateinit var prevTx: Hash
            var txIndex by Delegates.notNull<Int>()
            lateinit var tx: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    1 -> prevTxBlock = decodeHash(i)
                    2 -> prevTxIndex = decodeIntElement(descriptor, i)
                    3 -> prevTx = decodeHash(i)
                    4 -> txIndex = decodeIntElement(descriptor, i)
                    5 -> tx = decodeHash(i)
                    else -> throw SerializationException(
                        "Unknown index $i"
                    )
                }
            }
            endStructure(descriptor)
            transactionOutput(
                payout = payout, newTransaction = tx,
                newIndex = txIndex, previousBlock = prevTxBlock,
                previousIndex = prevTxIndex, previousTransaction = prevTx
            )
        }

    override fun serialize(encoder: Encoder, value: TransactionOutput) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0,
                PayoutSerializer, value.payout
            )
            encodeHash(1, value.prevTxBlock)
            encodeIntElement(descriptor, 2, value.prevTxIndex)
            encodeHash(3, value.prevTx)
            encodeIntElement(descriptor, 4, value.txIndex)
            encodeHash(5, value.tx)
            endStructure(descriptor)
        }
    }
}