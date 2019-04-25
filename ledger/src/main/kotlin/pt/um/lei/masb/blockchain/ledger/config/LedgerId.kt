package pt.um.lei.masb.blockchain.ledger.config

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import mu.KLogging
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.Hashed
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.ledger.emptyHash
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Hashable
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes
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