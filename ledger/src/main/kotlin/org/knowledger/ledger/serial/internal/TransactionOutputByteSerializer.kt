package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.core.serial.PublicKeySerializer
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.serial.TransactionOutputSerializer
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl
import java.security.PublicKey

@Serializer(forClass = TransactionOutput::class)
internal object TransactionOutputByteSerializer : KSerializer<TransactionOutput> {
    override val descriptor: SerialDescriptor =
        TransactionOutputSerializer.descriptor

    val hashsSerializer = HashSerializer.set

    override fun deserialize(decoder: Decoder): TransactionOutput =
        with(decoder.beginStructure(descriptor)) {
            lateinit var publicKey: PublicKey
            lateinit var previousCoinbase: Hash
            lateinit var payout: Payout
            lateinit var transactionHashes: MutableSet<Hash>
            lateinit var hash: Hash
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> publicKey = decodeSerializableElement(
                        descriptor, i, PublicKeySerializer
                    )
                    1 -> previousCoinbase = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    2 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    3 -> transactionHashes = decodeSerializableElement(
                        descriptor, i, hashsSerializer
                    ) as MutableSet<Hash>
                    4 -> hash = decodeSerializableElement(
                        descriptor, i, HashSerializer
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            HashedTransactionOutputImpl(
                TransactionOutputImpl(
                    publicKey = publicKey,
                    previousCoinbase = previousCoinbase,
                    _payout = payout,
                    _transactionHashes = transactionHashes
                ), hash
            )
        }

    override fun serialize(encoder: Encoder, obj: TransactionOutput) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, PublicKeySerializer, obj.publicKey
            )
            encodeSerializableElement(
                descriptor, 1, HashSerializer, obj.previousCoinbase
            )
            encodeSerializableElement(
                descriptor, 2, PayoutSerializer, obj.payout
            )
            encodeSerializableElement(
                descriptor, 3, hashsSerializer,
                obj.transactionHashes
            )
            encodeSerializableElement(
                descriptor, 4, HashSerializer, obj.hash
            )
            endStructure(descriptor)
        }
    }
}