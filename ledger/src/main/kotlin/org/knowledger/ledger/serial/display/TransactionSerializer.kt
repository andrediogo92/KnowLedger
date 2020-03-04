package org.knowledger.ledger.serial.display

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.hash.toEncoded
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.crypto.serial.EncodedSignatureSerializer
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.serial.internal.AbstractTransactionSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay
import java.security.PublicKey

/**
 * A pretty printing friendly serializer for transactions.
 * It encodes all byte data base64 encoded.
 */
internal object TransactionSerializer : AbstractTransactionSerializer(),
                                        HashEncodeForDisplay {
    override val publicKeyDescriptor: SerialDescriptor
        get() = EncodedPublicKeySerializer.descriptor

    override fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: PublicKey
    ) {
        encodeSerializableElement(
            descriptor, index, EncodedPublicKeySerializer,
            publicKey.toEncoded()
        )
    }

    override fun CompositeDecoder.decodePublicKey(
        index: Int
    ): PublicKey =
        decodeSerializableElement(
            descriptor, index, EncodedPublicKeySerializer
        ).toPublicKey()

    override val signatureDescriptor: SerialDescriptor
        get() = EncodedSignatureSerializer.descriptor

    override fun CompositeEncoder.encodeSignature(
        index: Int, encodedSignature: EncodedSignature
    ) {
        encodeSerializableElement(
            descriptor, index, EncodedSignatureSerializer,
            encodedSignature
        )
    }

    override fun CompositeDecoder.decodeSignature(
        index: Int
    ): EncodedSignature =
        decodeSerializableElement(
            descriptor, index, EncodedSignatureSerializer
        )
}