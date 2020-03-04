package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.display.HashDisplaySerializer

internal interface HashEncodeForDisplay : HashEncode {
    override val hashDescriptor: SerialDescriptor
        get() = HashDisplaySerializer.descriptor

    override fun CompositeEncoder.encodeHash(
        index: Int, hash: Hash
    ) {
        encodeSerializableElement(descriptor, index, HashDisplaySerializer, hash)
    }

    override fun CompositeDecoder.decodeHash(index: Int): Hash =
        decodeSerializableElement(descriptor, index, HashDisplaySerializer)
}