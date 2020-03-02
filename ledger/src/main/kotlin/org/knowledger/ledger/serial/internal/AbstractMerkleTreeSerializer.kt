package org.knowledger.ledger.serial.internal

import kotlinx.serialization.*
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.serial.HashAlgorithmSerializer
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.storage.MerkleTree

internal abstract class AbstractMerkleTreeSerializer : KSerializer<MerkleTree> {
    private object MerkleTreeSerialDescriptor : SerialClassDescImpl("MerkleTree") {
        init {
            addElement("collapsedTree")
            addElement("levelIndex")
            addElement("hasher")
        }
    }

    override val descriptor: SerialDescriptor = MerkleTreeSerialDescriptor


    abstract fun CompositeEncoder.encodeHashList(
        index: Int, hashList: List<Hash>
    )

    abstract fun CompositeDecoder.decodeHashList(
        index: Int
    ): List<Hash>


    override fun deserialize(decoder: Decoder): MerkleTree =
        with(decoder.beginStructure(descriptor)) {
            lateinit var collapsedTree: MutableList<Hash>
            lateinit var levelIndex: MutableList<Int>
            lateinit var hashers: Hashers
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> collapsedTree = decodeHashList(i) as MutableList<Hash>
                    1 -> levelIndex = decodeSerializableElement(
                        descriptor, i, IntSerializer.list
                    ) as MutableList<Int>
                    2 -> hashers = decodeSerializableElement(
                        descriptor, i, HashAlgorithmSerializer
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
            encodeHashList(0, obj.collapsedTree)
            encodeSerializableElement(
                descriptor, 1, IntSerializer.list, obj.levelIndex
            )
            encodeSerializableElement(
                descriptor, 2, HashAlgorithmSerializer, obj.hasher
            )
            endStructure(descriptor)
        }
    }
}