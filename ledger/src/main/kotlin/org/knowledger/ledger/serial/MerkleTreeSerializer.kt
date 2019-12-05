package org.knowledger.ledger.serial

import kotlinx.serialization.*
import kotlinx.serialization.internal.IntSerializer
import kotlinx.serialization.internal.SerialClassDescImpl
import kotlinx.serialization.internal.StringSerializer
import org.knowledger.collections.mapMutableList
import org.knowledger.ledger.core.base.hash.hashFromHexString
import org.knowledger.ledger.core.base.hash.toHexString
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.storage.MerkleTree

@Serializer(forClass = MerkleTree::class)
object MerkleTreeSerializer : KSerializer<MerkleTree> {
    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("MerkleTree") {
            init {
                addElement("collapsedTree")
                addElement("levelIndex")
                addElement("hash")
            }
        }

    override fun deserialize(decoder: Decoder): MerkleTree =
        with(decoder.beginStructure(descriptor)) {
            lateinit var collapsedTree: MutableList<Hash>
            lateinit var levelIndex: MutableList<Int>
            lateinit var hashers: Hashers
            loop@ while (true) {
                when (val i = decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> collapsedTree = decodeSerializableElement(
                        descriptor, i, StringSerializer.list
                    ).mapMutableList(String::hashFromHexString)
                    1 -> levelIndex = decodeSerializableElement(
                        descriptor, i, IntSerializer.list
                    ) as MutableList<Int>
                    2 -> hashers = decodeSerializableElement(
                        descriptor, i, Hashers.serializer()
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            endStructure(BlockHeaderSerializer.descriptor)
            MerkleTreeImpl(
                _collapsedTree = collapsedTree,
                _levelIndex = levelIndex,
                hasher = hashers
            )
        }

    override fun serialize(encoder: Encoder, obj: MerkleTree) {
        with(encoder.beginStructure(descriptor)) {
            encodeSerializableElement(
                descriptor, 0, StringSerializer.list,
                obj.collapsedTree.map { it.toHexString() }
            )
            encodeSerializableElement(
                descriptor, 1, IntSerializer.list, obj.levelIndex
            )
            encodeSerializableElement(
                descriptor, 2, Hashers.serializer(), obj.hasher
            )
        }
    }
}