package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import kotlinx.serialization.internal.IntSerializer
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Hashers
import org.knowledger.ledger.serial.MerkleTreeSerializer
import org.knowledger.ledger.storage.MerkleTree

@Serializer(forClass = MerkleTree::class)
internal object MerkleTreeByteSerializer : KSerializer<MerkleTree> {
    override val descriptor: SerialDescriptor =
        MerkleTreeSerializer.descriptor

    override fun deserialize(decoder: Decoder): MerkleTree =
        with(decoder.beginStructure(descriptor)) {
            lateinit var collapsedTree: MutableList<Hash>
            lateinit var levelIndex: MutableList<Int>
            lateinit var hashers: Hashers
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> collapsedTree = decodeSerializableElement(
                        descriptor, i, HashSerializer.list
                    ) as MutableList<Hash>
                    1 -> levelIndex = decodeSerializableElement(
                        descriptor, i, IntSerializer.list
                    ) as MutableList<Int>
                    2 -> hashers = decodeSerializableElement(
                        descriptor, i, Hashers.serializer()
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(descriptor)
            MerkleTreeImpl(
                _collapsedTree = collapsedTree,
                _levelIndex = levelIndex,
                hasher = hashers
            )
        }

    override fun serialize(encoder: Encoder, obj: MerkleTree) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, HashSerializer.list, obj.collapsedTree
            )
            encodeSerializableElement(
                descriptor, 1, IntSerializer.list, obj.levelIndex
            )
            encodeSerializableElement(
                descriptor, 2, Hashers.serializer(), obj.hasher
            )
            endStructure(descriptor)
        }
    }
}