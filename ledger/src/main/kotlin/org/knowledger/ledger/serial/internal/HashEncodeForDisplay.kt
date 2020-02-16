package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.serial.display.HashDisplaySerializer

internal interface HashEncodeForDisplay : HashEncode {
    override fun CompositeEncoder.encodeHash(
        index: Int, hash: Hash
    ) {
        encodeSerializableElement(descriptor, index, HashDisplaySerializer, hash)
    }

    override fun CompositeDecoder.decodeHash(index: Int): Hash =
        decodeSerializableElement(descriptor, index, HashDisplaySerializer)
}