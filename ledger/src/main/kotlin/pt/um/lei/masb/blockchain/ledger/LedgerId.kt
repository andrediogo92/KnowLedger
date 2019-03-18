package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.Hashable
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
import java.time.Instant
import java.util.*

data class LedgerId(
    val id: String,
    val uuid: UUID,
    val timestamp: Instant,
    val params: LedgerParams,
    var hash: Hash
) : Hashable, Storable, LedgerContract {

    internal constructor(
        id: String,
        uuid: UUID = UUID.randomUUID(),
        timestamp: Instant = Instant.now(),
        params: LedgerParams = LedgerParams()
    ) : this(id, uuid, timestamp, params, emptyHash()) {
        hash = digest(params.crypter)
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
                setProperty("hash", hash)
                setProperty("params", params.store(session))
            }

    override fun toString(): String = """
        |       LedgerId {
        |           UUID: $uuid
        |           Timestamp: $timestamp
        |           Id: $id
        |           Hash: ${hash.print()}
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
        if (!hash.contentEquals(other.hash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + hash.contentHashCode()
        return result
    }

    companion object : KLogging()
}