package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.serial.EncodedPublicKeyByteSerializer
import org.knowledger.ledger.serial.internal.AbstractWitnessSerializer
import org.knowledger.ledger.serial.internal.HashEncodeInBytes

internal object WitnessByteSerializer : AbstractWitnessSerializer(TransactionOutputByteSerializer),
                                        HashEncodeInBytes {
    override fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: EncodedPublicKey
    ) {
        encodeSerializableElement(
            descriptor, index, EncodedPublicKeyByteSerializer,
            publicKey
        )
    }

    override fun CompositeDecoder.decodePublicKey(
        index: Int
    ): EncodedPublicKey =
        decodeSerializableElement(
            descriptor, index, EncodedPublicKeyByteSerializer
        )
}