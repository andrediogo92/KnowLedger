package org.knowledger.ledger.serial.display

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.crypto.EncodedPublicKey
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.serial.internal.AbstractWitnessSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay

internal object WitnessSerializer : AbstractWitnessSerializer(TransactionOutputSerializer),
                                    HashEncodeForDisplay {
    override fun CompositeEncoder.encodePublicKey(
        index: Int, publicKey: EncodedPublicKey
    ) {
        encodeSerializableElement(
            descriptor, index, EncodedPublicKeySerializer,
            publicKey
        )
    }

    override fun CompositeDecoder.decodePublicKey(
        index: Int
    ): EncodedPublicKey =
        decodeSerializableElement(
            descriptor, index, EncodedPublicKeySerializer
        )
}