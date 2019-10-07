package org.knowledger.ledger.config.chainid

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.serial.HashSerializable
import org.knowledger.ledger.data.Hash
import org.knowledger.ledger.data.Tag

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