package pt.um.lei.masb.blockchain

import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.DummyData
import pt.um.lei.masb.blockchain.data.MerkleTree
import pt.um.lei.masb.blockchain.persistance.BlockChainTransactions
import java.math.BigInteger
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*


/**
 * Create a geographically unbounded blockchain.
 */
class BlockChain(val id: String) {
    companion object : KLogging() {
        const val RECALC_TIME = 1228800

        const val CACHE_SIZE = 40
        const val RECALC_TRIGGER = 2048


        //Get a specific blockchain from DB
        fun getBlockChainById(id: Long): BlockChain? = BlockChainTransactions().getBlockChain(id)

        fun getFirstBlockChain(): BlockChain? = BlockChainTransactions().getBlockChain()
    }

    val blockChainId = BlockChainId(UUID.randomUUID(), Instant.now(), id)

    val originHeader = BlockHeader(blockChainId,
                                   BigInteger.ZERO,
                                   0,
                                   "0",
                                   "0",
                                   "",
                                   ZonedDateTime.of(2018, 3, 13, 0, 0, 0, 0, ZoneOffset.UTC)
                                       .toInstant(),
                                   0.toLong())

    val origin: Block<DummyData> = Block(mutableListOf(),
                                         Coinbase(blockChainId),
                                         originHeader,
                                         MerkleTree())


    private val _sidechains: MutableMap<Class<*>, SideChain<*>> = mutableMapOf()
    val sidechains: Map<Class<*>, SideChain<*>>
        get() = _sidechains

    fun <T : BlockChainData<T>> getSideChainOf(clazz: Class<T>): SideChain<T>? =
            sidechains[clazz] as SideChain<T>

    fun <T : BlockChainData<T>> registerSideChainOf(clazz: Class<T>): BlockChain =
            if (!sidechains.keys.contains(clazz)) {
                _sidechains[clazz] = SideChain(clazz.kotlin, blockChainId)
                this
        } else {
                this
            }

}
