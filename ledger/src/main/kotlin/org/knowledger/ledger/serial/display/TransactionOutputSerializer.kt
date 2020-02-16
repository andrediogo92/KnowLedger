package org.knowledger.ledger.serial.display

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.hash.toEncoded
import org.knowledger.ledger.crypto.serial.EncodedPublicKeySerializer
import org.knowledger.ledger.crypto.toPublicKey
import org.knowledger.ledger.serial.internal.AbstractTransactionOutputSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay
import java.security.PublicKey

internal object TransactionOutputSerializer : AbstractTransactionOutputSerializer(HashSerializer),
                                              HashEncodeForDisplay {
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
}