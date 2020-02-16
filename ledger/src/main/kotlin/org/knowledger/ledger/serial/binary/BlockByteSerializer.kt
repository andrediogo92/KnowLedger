package org.knowledger.ledger.serial.binary

import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.CompositeEncoder
import org.knowledger.ledger.serial.internal.AbstractBlockSerializer
import org.knowledger.ledger.storage.BlockHeader
import org.knowledger.ledger.storage.Coinbase
import org.knowledger.ledger.storage.MerkleTree

internal object BlockByteSerializer : AbstractBlockSerializer(TransactionByteSerializer) {
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