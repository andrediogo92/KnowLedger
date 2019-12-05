@file:UseSerializers(HashSerializer::class)
package org.knowledger.ledger.config.chainid

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.hash.Hash
import org.knowledger.ledger.crypto.hash.Hasher
import org.knowledger.ledger.data.Tag
import org.knowledger.ledger.serial.HashSerializable

internal data class ChainIdImpl internal constructor(
    override val tag: Tag,
    override val ledgerHash: Hash,
    override val hash: Hash
) : ChainId {
    internal constructor(
        hasher: Hasher, encoder: BinaryFormat,
        tag: Tag, ledgerHash: Hash
    ) : this(
        tag, ledgerHash,
        generateChainHandleHash(
            hasher,
            encoder,
            tag,
            ledgerHash
        )
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChainId) return false

        if (tag != other.tag) return false
        if (ledgerHash != other.ledgerHash) return false
        if (hash != other.hash) return false

        return true
    }

    override fun hashCode(): Int {
        var result = tag.hashCode()
        result = 31 * result + ledgerHash.hashCode()
        result = 31 * result + hash.hashCode()
        return result
    }


    @Serializable
    private data class ChainIdBuilder(
        val tag: Tag, val ledgerHash: Hash
    ) : HashSerializable {
        override fun serialize(encoder: BinaryFormat): ByteArray =
            encoder.dump(serializer(), this)
    }


    companion object {
        private fun generateChainHandleHash(
            hasher: Hasher, encoder: BinaryFormat,
            tag: Tag, ledgerHash: Hash
        ): Hash =
            ChainIdBuilder(tag, ledgerHash).digest(
                hasher, encoder
            )
    }
}