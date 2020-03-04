package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.list
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.internal.AbstractMerkleTreeSerializer

internal object MerkleTreeByteSerializer : AbstractMerkleTreeSerializer() {
    override val hashListDescriptor: SerialDescriptor
        get() = HashSerializer.list.descriptor

    override fun CompositeEncoder.encodeHashList(
        index: Int, hashList: List<Hash>
    ) {
        encodeSerializableElement(
            descriptor, index, HashSerializer.list,
            hashList
        )
    }

    override fun CompositeDecoder.decodeHashList(
        index: Int
    ): List<Hash> =
        decodeSerializableElement(
            descriptor, index, HashSerializer.list
        )
}