package pt.um.lei.masb.blockchain

import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import java.time.Instant
import java.util.*

data class BlockChainId(val uuid: UUID, val timestamp: Instant, val id: String) {
    val hash = DEFAULT_CRYPTER.applyHash("$id$uuid$timestamp")
}