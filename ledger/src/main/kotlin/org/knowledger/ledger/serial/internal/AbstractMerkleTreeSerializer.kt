package org.knowledger.ledger.serial.internal

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.list
import kotlinx.serialization.builtins.serializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.serial.HashAlgorithmSerializer
import org.knowledger.ledger.crypto.storage.MerkleTreeImpl
import org.knowledger.ledger.storage.MerkleTree

internal abstract class AbstractMerkleTreeSerializer : KSerializer<MerkleTree> {
    private val intListSerializer = Int.serializer().list
    override val descriptor: SerialDescriptor =
        SerialDescriptor("MerkleTree") {
            element(
                elementName = "collapsedTree",
                descriptor = hashListDescriptor
            )
            element(
                elementName = "levelIndex",
                descriptor = intListSerializer.descriptor
            )
            element(
                elementName = "hasher",
                descriptor = HashAlgorithmSerializer.descriptor
            )

        }


    abstract val hashListDescriptor: SerialDescriptor
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
                        descriptor, i, intListSerializer
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

    override fun serialize(encoder: Encoder, value: MerkleTree) {
        with(encoder.beginStructure(descriptor)) {
            encodeHashList(0, value.collapsedTree)
            encodeSerializableElement(
                descriptor, 1, intListSerializer, value.levelIndex
            )
            encodeSerializableElement(
                descriptor, 2, HashAlgorithmSerializer, value.hasher
            )
            endStructure(descriptor)
        }
    }
}