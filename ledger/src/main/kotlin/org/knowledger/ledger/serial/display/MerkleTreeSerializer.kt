package org.knowledger.ledger.serial.display

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.builtins.list
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.serial.internal.AbstractMerkleTreeSerializer

internal object MerkleTreeSerializer : AbstractMerkleTreeSerializer() {
    override val hashListDescriptor: SerialDescriptor
        get() = HashDisplaySerializer.list.descriptor

    override fun CompositeEncoder.encodeHashList(
        index: Int, hashList: List<Hash>
    ) {
        encodeSerializableElement(
            descriptor, index, HashDisplaySerializer.list,
            hashList
        )
    }

    override fun CompositeDecoder.decodeHashList(
        index: Int
    ): List<Hash> =
        decodeSerializableElement(
            descriptor, index, HashDisplaySerializer.list
        )
}