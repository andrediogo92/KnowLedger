@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.storage.config.ledger

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash

@Serializable
@SerialName("LedgerParams")
data class ImmutableLedgerParams(
    override val hashers: Hash,
    override val recalculationTime: Long,
    override val recalculationTrigger: Int
) : LedgerParams {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerParams) return false

        if (hashers != other.hashers) return false
        if (recalculationTime != other.recalculationTime) return false
        if (recalculationTrigger != other.recalculationTrigger) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hashers.hashCode()
        result = 31 * result + recalculationTime.hashCode()
        result = 31 * result + recalculationTrigger
        return result
    }
}