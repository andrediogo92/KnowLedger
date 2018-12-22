package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import mu.KLogging
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import java.time.Instant
import java.util.*

data class BlockChainId(
    val uuid: UUID,
    val timestamp: Instant,
    val id: String
) : Hashable, Storable, BlockChainContract {
    val hash = digest(crypter)

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $uuid
            ${timestamp.epochSecond}
            ${timestamp.nano}
            $id
        """.trimIndent()
        )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("BlockChainId")
            .apply {
                this.setProperty(
                    "uuid",
                    uuid.toString()
                )
                this.setProperty(
                    "timestamp",
                    timestamp.toString()
                )
                this.setProperty(
                    "id",
                    id
                )
                this.setProperty(
                    "hash",
                    ORecordBytes(hash)
                )
            }

    override fun toString(): String = """
        |       BlockChainId {
        |           UUID: $uuid
        |           Timestamp: $timestamp
        |           Id: $id
        |           Hash: ${hash.print()}
        |       }
    """.trimMargin()

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}