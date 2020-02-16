package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.serial.PublicKeySerializer
import org.knowledger.ledger.serial.internal.AbstractTransactionOutputSerializer
import org.knowledger.ledger.serial.internal.HashEncodeForDisplay
import java.security.PublicKey

internal object TransactionOutputByteSerializer : AbstractTransactionOutputSerializer(HashSerializer),
                                                  HashEncodeForDisplay {
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
}