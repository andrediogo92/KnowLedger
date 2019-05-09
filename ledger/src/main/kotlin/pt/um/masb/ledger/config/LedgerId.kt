package pt.um.masb.ledger.config

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import mu.KLogging
import pt.um.masb.common.Hash
import pt.um.masb.common.Hashable
import pt.um.masb.common.Hashed
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.emptyHash
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.print
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.LedgerContract
import java.time.Instant
import java.util.*

@JsonClass(generateAdapter = true)
data class LedgerId(
    val id: String,
    val uuid: UUID,
    val timestamp: Instant,
    val params: LedgerParams,
    override var hashId: Hash
) : Hashable, Hashed, Storable, LedgerContract {

    internal constructor(
        id: String,
        uuid: UUID = UUID.randomUUID(),
        timestamp: Instant = Instant.now(),
        params: LedgerParams = LedgerParams()
    ) : this(id, uuid, timestamp, params, emptyHash()) {
        hashId = digest(params.crypter)
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                uuid.toString().toByteArray(),
                timestamp.epochSecond.bytes(),
                timestamp.nano.bytes(),
                id.toByteArray(),
                params.crypter.id,
                params.blockParams.blockLength.bytes(),
                params.blockParams.blockMemSize.bytes(),
                params.recalcTrigger.bytes(),
                params.recalcTime.bytes()
            )
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("LedgerId")
            .apply {
                setProperty("uuid", uuid.toString())
                setProperty("timestamp", timestamp.toString())
                setProperty("id", id)
                setProperty("hashId", hashId)
                setProperty("params", params.store(session))
            }

    override fun toString(): String = """
        |       LedgerId {
        |           UUID: $uuid
        |           Timestamp: $timestamp
        |           Id: $id
        |           Hash: ${hashId.print()}
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

    companion object : KLogging()
}