@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.config.chainid

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.config.ChainId
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.data.Tag

@Serializable
data class ImmutableChainId(
    override val tag: Tag,
    override val ledgerHash: Hash,
    override val hash: Hash
) : ChainId {
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
}