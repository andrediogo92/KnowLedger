package pt.um.lei.masb.blockchain

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.HumidityData
import pt.um.lei.masb.blockchain.data.LuminosityData
import pt.um.lei.masb.blockchain.data.NoiseData
import pt.um.lei.masb.blockchain.data.OtherData
import pt.um.lei.masb.blockchain.data.TemperatureData
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.Storable
import java.math.BigDecimal
import java.math.BigInteger
import java.time.Instant
import java.util.*


/**
 * Create a geographically unbounded blockchain.
 */
class BlockChain(
    val pw: PersistenceWrapper =
        PersistenceWrapper.DEFAULT_DB,
    val blockChainId: BlockChainId,
    private val categories: MutableMap<String, CategoryTypes> =
        mutableMapOf(
            "Noise" to CategoryTypes(
                mutableListOf(
                    NoiseData::class.qualifiedName as String
                )
            ),
            "Temperature" to CategoryTypes(
                mutableListOf(
                    TemperatureData::class.qualifiedName as String
                )
            ),
            "Humidity" to CategoryTypes(
                mutableListOf(
                    HumidityData::class.qualifiedName as String
                )
            ),
            "Luminosity" to CategoryTypes(
                mutableListOf(
                    LuminosityData::class.qualifiedName as String
                )
            ),
            "Other" to CategoryTypes(
                mutableListOf(
                    OtherData::class.qualifiedName as String
                )
            )
        ),
    private val internalSidechains: MutableMap<String, SideChain> =
        mutableMapOf()
) : Storable, BlockChainContract {

    init {
        pw.registerDefaultClusters(
            blockChainId.hash
        )
    }


    val sidechains: Map<String, SideChain>
        get() = internalSidechains

    constructor(
        pw: PersistenceWrapper =
            PersistenceWrapper.DEFAULT_DB,
        id: String
    ) : this(
        pw,
        BlockChainId(
            UUID.randomUUID(),
            Instant.now(),
            id
        )
    )

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("BlockChain")
            .apply {
                this.setProperty(
                    "blockChainId",
                    blockChainId.store(session)
                )
                this.setProperty(
                    "categories",
                    categories
                )
                this.setProperty(
                    "sidechains",
                    sidechains.mapValues {
                        it.value.store(session)
                    }
                )
            }

    fun <T : BlockChainData> getCategoryOf(
        s: Class<T>
    ): String? {
        val name = s::class.qualifiedName
        return categories.keys.find { cat ->
            categories[cat]?.categoryTypes?.any {
                it == name
            } ?: false
        }
    }

    fun getClassesInCategory(
        s: String
    ): List<String> =
        categories[s]?.categoryTypes ?: emptyList()

    fun <T : BlockChainData> getSideChainOf(
        clazz: Class<T>
    ): SideChain? =
        sidechains[clazz.name]


    fun <T : BlockChainData> registerSideChainOf(
        clazz: Class<T>,
        category: String = "Misc"
    ): BlockChain =
        if (!sidechains.keys.contains(clazz.name)) {
            internalSidechains[clazz.name] = SideChain(
                pw,
                clazz.name,
                blockChainId.hash
            )
            this
        } else {
            logger.info {
                """Attempt to insert class already existent:
                 |  ${clazz.canonicalName}""".trimMargin()
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
        fun getBlockChainById(
            pw: PersistenceWrapper =
                PersistenceWrapper.DEFAULT_DB,
            id: BlockChainId
        ): BlockChain? =
            pw.getBlockChain(id)

        fun getBlockChainByHash(
            pw: PersistenceWrapper =
                PersistenceWrapper.DEFAULT_DB,
            hash: Hash
        ): BlockChain? =
            pw.getBlockChain(hash)
    }

}
