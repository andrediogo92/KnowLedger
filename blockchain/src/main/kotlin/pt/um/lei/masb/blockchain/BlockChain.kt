package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.*
import pt.um.lei.masb.blockchain.persistance.getBlockChain
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.*


/**
 * Create a geographically unbounded blockchain.
 */
class BlockChain(
    val id: String
) : Storable {


    val blockChainId = BlockChainId(
        UUID.randomUUID(),
        Instant.now(),
        id
    )


    val originHeader = BlockHeader(
        blockChainId,
        MAX_DIFFICULTY,
        0,
        "0",
        "0",
        "",
        ZonedDateTime
            .of(
                2018,
                3,
                13,
                0,
                0,
                0,
                0,
                ZoneOffset.UTC
            )
            .toInstant(),
        0.toLong()
    )


    val origin: Block = Block(
        mutableListOf(),
        Coinbase(),
        originHeader,
        MerkleTree()
    )


    private val categories = mutableMapOf(
        "Noise" to mutableListOf(NoiseData::class.java),
        "Temperature" to mutableListOf(TemperatureData::class.java),
        "Humidity" to mutableListOf(HumidityData::class.java),
        "Luminosity" to mutableListOf(LuminosityData::class.java),
        "Other" to mutableListOf(OtherData::class.java)
    )


    private val _sidechains = mutableMapOf<Class<*>, SideChain>()
    val sidechains: Map<Class<*>, SideChain>
        get() = _sidechains


    private val _loaders = mutableMapOf<Class<*>, Loadable<*>>()
    val loaders: Map<Class<*>, Loadable<*>>
        get() = _loaders


    override fun store(): OElement {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun <T : BlockChainData> getCategoryOf(
        s: Class<T>
    ): String? =
        categories.keys.find { cat ->
            categories[cat]?.any { it == s } ?: false
        }

    fun getClassesInCategory(
        s: String
    ): List<Class<*>> =
        categories[s] ?: emptyList()

    fun <T : BlockChainData> getSideChainOf(
        clazz: Class<T>
    ): SideChain? =
        sidechains[clazz]

    fun <T : BlockChainData> getLoaderOf(
        clazz: Class<T>
    ): Loadable<T>? =
        loaders[clazz] as Loadable<T>?

    fun <T : BlockChainData> registerSideChainOf(
        clazz: Class<T>,
        category: String = "Misc",
        loadable: Loadable<T>
    ): BlockChain =
        if (!sidechains.keys.contains(clazz)) {
            _sidechains[clazz] =
                    SideChain(clazz.kotlin, blockChainId)
            _loaders[clazz] =
                    loadable
            this
        } else {
            logger.info {
                "Attempt to insert class already existent: ${clazz.canonicalName}"
            }
            this
        }

    companion object : KLogging() {
        const val CACHE_SIZE = 40

        val RECALC_DIV = BigInteger("10000000000000")
        val RECALC_MULT = BigDecimal("10000000000000")
        const val RECALC_TIME = 1228800000
        const val RECALC_TRIGGER = 2048

        //Get a specific blockchain from DB
        fun getBlockChainById(id: BlockChainId): BlockChain? =
            getBlockChain(id)

        fun getBlockChainByHash(hash: Hash): BlockChain? =
            getBlockChain(hash)
    }

}
