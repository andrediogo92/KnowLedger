package org.knowledger.ledger.config.chainid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.data.Tag
import org.knowledger.ledger.core.hash.Hash
import org.knowledger.ledger.core.hash.Hasher
import org.knowledger.ledger.core.serial.HashSerializable

@Serializable
@SerialName("ChainId")
data class StorageUnawareChainId internal constructor(
    override val tag: Tag,
    override val ledgerHash: Hash,
    override val hash: Hash
) : ChainId {
    internal constructor(
        hasher: Hasher,
        cbor: Cbor,
        tag: Tag,
        ledgerHash: Hash
    ) : this(
        tag, ledgerHash,
        generateChainHandleHash(
            hasher,
            cbor,
            tag,
            ledgerHash
        )
    )

    @Serializable
    private data class ChainIdBuilder(
        val tag: Tag, val ledgerHash: Hash
    ) : HashSerializable {
        override fun serialize(cbor: Cbor): ByteArray =
            cbor.dump(serializer(), this)
    }


    companion object {
        private fun generateChainHandleHash(
            hasher: Hasher,
            cbor: Cbor,
            tag: Tag,
            ledgerHash: Hash
        ): Hash =
            ChainIdBuilder(tag, ledgerHash).digest(
                hasher, cbor
            )
    }

}