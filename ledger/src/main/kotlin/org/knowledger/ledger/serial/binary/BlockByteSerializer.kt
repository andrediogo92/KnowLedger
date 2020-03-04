package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.serial.internal.AbstractBlockSerializer
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree

internal object BlockByteSerializer : AbstractBlockSerializer(TransactionByteSerializer) {
    override val coinbaseDescriptor: SerialDescriptor
        get() = CoinbaseByteSerializer.descriptor

    override fun CompositeEncoder.encodeCoinbase(
        index: Int, coinbase: Coinbase
    ) {
        encodeSerializableElement(
            descriptor, index,
            CoinbaseByteSerializer, coinbase
        )
    }

    override fun CompositeDecoder.decodeCoinbase(
        index: Int
    ): Coinbase =
        decodeSerializableElement(
            descriptor, index,
            CoinbaseByteSerializer
        )

    override val blockHeaderDescriptor: SerialDescriptor
        get() = BlockHeaderByteSerializer.descriptor

    override fun CompositeEncoder.encodeBlockHeader(
        index: Int, header: BlockHeader
    ) {
        encodeSerializableElement(
            descriptor, index,
            BlockHeaderByteSerializer, header
        )
    }

    override fun CompositeDecoder.decodeBlockHeader(
        index: Int
    ): BlockHeader =
        decodeSerializableElement(
            descriptor, index,
            BlockHeaderByteSerializer
        )

    override val merkleTreeDescriptor: SerialDescriptor
        get() = MerkleTreeByteSerializer.descriptor

    override fun CompositeEncoder.encodeMerkleTree(
        index: Int, merkleTree: MerkleTree
    ) {
        encodeSerializableElement(
            descriptor, index,
            MerkleTreeByteSerializer, merkleTree
        )
    }

    override fun CompositeDecoder.decodeMerkleTree(
        index: Int
    ): MerkleTree =
        decodeSerializableElement(
            descriptor, index,
            MerkleTreeByteSerializer
        )
}