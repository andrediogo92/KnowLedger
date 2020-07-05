@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.config.chainid.factory

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.config.chainid.ImmutableChainId
import org.knowledger.ledger.core.calculateHash
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.serial.HashSerializable

object ChainIdFactoryImpl : ChainIdFactory {
    @Serializable
    private data class ChainIdBuilder(
        val tag: Tag, val ledgerHash: Hash
    ) : HashSerializable {
        override fun serialize(encoder: BinaryFormat): ByteArray =
            encoder.dump(serializer(), this)
    }

    private fun generateChainHandleHash(
        hasher: Hashers, encoder: BinaryFormat,
        tag: Tag, ledgerHash: Hash
    ): Hash = ChainIdBuilder(tag, ledgerHash).calculateHash(
        hasher, encoder
    )

    override fun create(tag: Tag, ledgerHash: Hash, hash: Hash): ChainId =
        ImmutableChainId(tag, ledgerHash, hash)

    override fun create(
        hasher: Hashers, encoder: BinaryFormat,
        tag: Tag, ledgerHash: Hash
    ): ChainId = create(
        tag, ledgerHash,
        generateChainHandleHash(hasher, encoder, tag, ledgerHash)
    )

    override fun create(other: ChainId): ChainId =
        with(other) { create(tag, ledgerHash, hash) }

}