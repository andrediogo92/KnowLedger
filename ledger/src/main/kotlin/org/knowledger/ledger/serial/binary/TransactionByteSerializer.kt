package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.crypto.EncodedSignature
import org.knowledger.ledger.crypto.serial.EncodedSignatureByteSerializer
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.serial.internal.AbstractTransactionSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes
import java.security.PublicKey

/**
 * A pretty printing friendly serializer for transactions.
 * It encodes all byte data base64 encoded.
 */
internal object TransactionByteSerializer : AbstractTransactionSerializer(),
                                            HashEncodeInBytes {
    override fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: PublicKey
    ) {
        encodeSerializableElement(
            descriptor, index, PublicKeySerializer,
            publicKey
        )
    }

    override fun CompositeDecoder.decodePublicKey(
        index: Int
    ): PublicKey =
        decodeSerializableElement(
            descriptor, index, PublicKeySerializer
        )


    override fun CompositeEncoder.encodeSignature(
        index: Int, encodedSignature: EncodedSignature
    ) {
        encodeSerializableElement(
            descriptor, index, EncodedSignatureByteSerializer,
            encodedSignature
        )
    }

    override fun CompositeDecoder.decodeSignature(
        index: Int
    ): EncodedSignature =
        decodeSerializableElement(
            descriptor, index, EncodedSignatureByteSerializer
        )
}