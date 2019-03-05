package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.Hashable
import java.time.Instant
import java.util.*

data class LedgerId internal constructor(
    val uuid: UUID,
    val timestamp: Instant,
    val id: String,
    val params: LedgerParams,
    private var internalHash: Hash
) : Hashable, Storable, LedgerContract {
    val hash: Hash
        get() = internalHash

    internal constructor(
        uuid: UUID,
        timestamp: Instant,
        id: String,
        params: LedgerParams
    ) : this(uuid, timestamp, id, params, emptyHash()) {
        internalHash = digest(params.crypter)
    }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $uuid
            ${timestamp.epochSecond}
            ${timestamp.nano}
            $id
            ${params.crypter.id}
            ${params.blockLength}
            ${params.blockMemSize}
            ${params.recalcTrigger}
            ${params.recalcTime}
        """.trimIndent()
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
                setProperty("internalHash", internalHash)
                setProperty("params", params.store(session))
            }

    override fun toString(): String = """
        |       LedgerId {
        |           UUID: $uuid
        |           Timestamp: $timestamp
        |           Id: $id
        |           Hash: ${internalHash.print()}
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
        if (!internalHash.contentEquals(other.internalHash)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = uuid.hashCode()
        result = 31 * result + timestamp.hashCode()
        result = 31 * result + id.hashCode()
        result = 31 * result + params.hashCode()
        result = 31 * result + internalHash.contentHashCode()
        return result
    }

    companion object : KLogging()
}