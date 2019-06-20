package pt.um.masb.ledger.config

import com.squareup.moshi.JsonClass
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hash.Companion.emptyHash
import pt.um.masb.common.hash.Hashable
import pt.um.masb.common.hash.Hashed
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.LedgerContract
import java.time.Instant
import java.util.*

@JsonClass(generateAdapter = true)
data class LedgerId(
    val id: String,
    val uuid: UUID,
    val timestamp: Instant,
    val params: LedgerParams,
    internal var hash: Hash
) : Hashable, Hashed, LedgerContract {
    override val hashId: Hash
        get() = hash


    internal constructor(
        id: String,
        params: LedgerParams,
        uuid: UUID = UUID.randomUUID(),
        timestamp: Instant = Instant.now()
    ) : this(id, uuid, timestamp, params, emptyHash) {
        hash = digest(
            AvailableHashAlgorithms.getHasher(params.crypter)
        )
    }

    override fun digest(c: Hasher): Hash =
        c.applyHash(
            flattenBytes(
                uuid.toString().toByteArray(),
                timestamp.epochSecond.bytes(),
                timestamp.nano.bytes(),
                id.toByteArray(),
                params.crypter.bytes,
                params.blockParams.blockLength.bytes(),
                params.blockParams.blockMemSize.bytes(),
                params.recalcTrigger.bytes(),
                params.recalcTime.bytes()
            )
        )


    override fun toString(): String = """
        |       LedgerId {
        |           UUID: $uuid
        |           Timestamp: $timestamp
        |           Id: $id
        |           Hash: ${hashId.print}
        |
        |       }
    """.trimMargin()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is LedgerId) return false

        if (uuid != other.uuid) return false
        if (timestamp != other.timestamp) return false
        if (id != other.id) return false
        if (params != other.params) return false
        if (!hashId.contentEquals(other.hashId)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + hashId.contentHashCode()
        return result
    }

}