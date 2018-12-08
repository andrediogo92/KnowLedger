package pt.um.lei.masb.blockchain

import mu.KLogging
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable
import java.time.Instant
import java.util.*

data class BlockChainId(
    val uuid: UUID,
    val timestamp: Instant,
    val id: String
) : Hashable {
    val hash = digest(crypter)

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
            $uuid
            $timestamp
            $id
        """.trimIndent()
        )

    override fun toString(): String = """
        |       BlockChainId {
        |           UUID: $uuid
        |           Timestamp: $timestamp
        |           Id: $id
        |           Hash: $hash
        |       }
    """.trimMargin()

    companion object : KLogging() {
        val crypter = DEFAULT_CRYPTER
    }
}