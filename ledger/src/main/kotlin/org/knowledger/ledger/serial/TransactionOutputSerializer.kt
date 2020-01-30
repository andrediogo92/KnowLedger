package org.knowledger.ledger.serial

import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.StringSerializer
import org.knowledger.collections.mapMutableSet
import org.knowledger.collections.mapToSet
import org.knowledger.ledger.core.base.hash.hashFromHexString
import org.knowledger.ledger.core.serial.PayoutSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.toEncoded
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.data.Payout
import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.transaction.output.HashedTransactionOutputImpl
import org.knowledger.ledger.storage.transaction.output.TransactionOutputImpl
import java.security.PublicKey

@Serializer(forClass = TransactionOutput::class)
object TransactionOutputSerializer : KSerializer<TransactionOutput> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("TransactionOutput") {
            init {
                addElement("publicKey")
                addElement("previousCoinbase")
                addElement("payout")
                addElement("transactionHashes")
                addElement("hash")
            }
        }

    val hashsSerializer = StringSerializer.set

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
                        descriptor, i, EncodedPublicKeySerializer
                    ).toPublicKey()
                    1 -> previousCoinbase = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
                    2 -> payout = decodeSerializableElement(
                        descriptor, i, PayoutSerializer
                    )
                    3 -> transactionHashes = decodeSerializableElement(
                        descriptor, i, hashsSerializer
                    ).mapMutableSet { it.hashFromHexString() }
                    4 -> hash = decodeStringElement(
                        descriptor, i
                    ).hashFromHexString()
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
                descriptor, 0, EncodedPublicKeySerializer,
                obj.publicKey.toEncoded()
            )
            encodeStringElement(
                descriptor, 1, obj.previousCoinbase.toHexString()
            )
            encodeSerializableElement(
                descriptor, 2, PayoutSerializer, obj.payout
            )
            encodeSerializableElement(
                descriptor, 3, hashsSerializer,
                obj.transactionHashes.mapToSet { it.toHexString() }
            )
            encodeStringElement(
                descriptor, 4, obj.hash.toHexString()
            )
            endStructure(descriptor)
        }
    }
}