package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.transactionOutput
import kotlin.properties.Delegates

internal abstract class AbstractTransactionOutputSerializer : KSerializer<TransactionOutput>, HashEncode {
    private object TransactionOutputSerialDescriptor : SerialClassDescImpl("TransactionOutput") {
        init {
            addElement("payout")
            addElement("prevTxBlock")
            addElement("prevTxIndex")
            addElement("prevTx")
            addElement("txIndex")
            addElement("tx")
        }
    }

    override val descriptor: SerialDescriptor = TransactionOutputSerialDescriptor


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

    override fun serialize(encoder: Encoder, obj: TransactionOutput) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0,
                PayoutSerializer, obj.payout
            )
            encodeHash(1, obj.prevTxBlock)
            encodeIntElement(descriptor, 2, obj.prevTxIndex)
            encodeHash(3, obj.prevTx)
            encodeIntElement(descriptor, 4, obj.txIndex)
            encodeHash(5, obj.tx)
            endStructure(descriptor)
        }
    }
}