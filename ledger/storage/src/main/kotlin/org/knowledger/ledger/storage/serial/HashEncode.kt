package org.knowledger.ledger.storage.serial

import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.CompositeDecoder
import kotlinx.serialization.encoding.CompositeEncoder
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash

interface HashEncode {
    val descriptor: SerialDescriptor
    val hashDescriptor: SerialDescriptor get() = HashSerializer.descriptor

    fun CompositeEncoder.encodeHash(index: Int, hash: Hash) {
        encodeSerializableElement(descriptor, index, HashSerializer, hash)
    }

    fun CompositeDecoder.decodeHash(index: Int): Hash =
        decodeSerializableElement(descriptor, index, HashSerializer)
}