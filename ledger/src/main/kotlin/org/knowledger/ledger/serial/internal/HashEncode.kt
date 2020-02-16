package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.crypto.hash.Hash

internal interface HashEncode {
    val descriptor: SerialDescriptor

    fun CompositeEncoder.encodeHash(index: Int, hash: Hash)
    fun CompositeDecoder.decodeHash(index: Int): Hash
}