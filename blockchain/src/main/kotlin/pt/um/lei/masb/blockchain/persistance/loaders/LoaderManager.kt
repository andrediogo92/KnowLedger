package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.BlockChainContract
import pt.um.lei.masb.blockchain.BlockChainId
import pt.um.lei.masb.blockchain.CategoryTypes
import pt.um.lei.masb.blockchain.ChainLoadable
import pt.um.lei.masb.blockchain.DefaultLoadable
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.Loadable
import java.time.Instant
import java.util.*

object LoaderManager {

    private val dataLoaders: MutableMap<Hash, Loaders> =
        mutableMapOf()

    internal val idLoader: (OElement) -> BlockChainId = {
        val uuid = UUID.fromString(
            it.getProperty<String>("uuid")
        )
        val timestamp = Instant.ofEpochSecond(
            it.getProperty<Long>("seconds"),
            it.getProperty<Int>("nanos").toLong()
        )
        val id = it.getProperty<String>("id")
        BlockChainId(uuid, timestamp, id)
    }

    internal val categoryLoader: (OElement) -> CategoryTypes =
        {
            CategoryTypes(
                it.getProperty<List<String>>(
                    "categoryTypes"
                )
            )
        }


    @Suppress("UNCHECKED_CAST")
    internal fun <T : BlockChainContract> getFromDefault(
        id: String
    ): DefaultLoadable<T> =
        BlockChainLoaders.defaults[id] as DefaultLoadable<T>

    @Suppress("UNCHECKED_CAST")
    internal fun <T : BlockChainContract> getFromChains(
        id: String
    ): ChainLoadable<T> =
        BlockChainLoaders.chains[id] as ChainLoadable<T>


    @Suppress("UNCHECKED_CAST")
    internal fun getFromLoaders(
        blockChainId: Hash,
        id: String
    ): Loadable<BlockChainData>? =
        dataLoaders[blockChainId]
            ?.loaders
            ?.get(id) as Loadable<BlockChainData>

    fun <T : BlockChainData> registerLoader(
        blockChainId: Hash,
        loaderType: String,
        loader: Loadable<T>
    ) {
        if (dataLoaders.containsKey(blockChainId)) {
            dataLoaders[blockChainId]!!.loaders +=
                    (loaderType to loader)
        } else {
            dataLoaders[blockChainId] =
                    PluggableLoaders(
                        mutableMapOf(
                            loaderType to loader
                        )
                    )
        }
    }

    fun registerLoaders(
        blockChainId: Hash,
        loaders: Loaders
    ) {
        if (dataLoaders.containsKey(blockChainId)) {
            dataLoaders[blockChainId]!!.loaders +=
                    loaders.loaders
        } else {
            dataLoaders[blockChainId] = loaders
        }
    }

}