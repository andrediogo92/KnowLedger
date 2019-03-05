package pt.um.lei.masb.blockchain.persistance.loaders

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.Loadable
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.ledger.LedgerId
import pt.um.lei.masb.blockchain.ledger.LedgerParams
import pt.um.lei.masb.blockchain.ledger.print
import pt.um.lei.masb.blockchain.service.ServiceHandle
import pt.um.lei.masb.blockchain.service.results.DataResult
import pt.um.lei.masb.blockchain.service.results.intoData
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.base64encode
import java.time.Instant
import java.util.*

object LoaderManager {
    val crypters = mutableMapOf(
        base64encode(DEFAULT_CRYPTER.id.hash) to DEFAULT_CRYPTER
    )

    private val dataLoaders: MutableMap<Hash, Loaders> =
        mutableMapOf()

    internal val paramLoader: (Crypter, Hash, OElement) -> DataResult<LedgerParams> =
        { crypter, hash, elem ->
            try {
                val crypterHash = Hash(elem.getProperty<ByteArray>("crypter"))
                if (crypter.id.hash.contentEquals(crypterHash.hash)) {
                    DataResult.NonMatchingCrypter(
                        """Non matching crypter at load params:
                            | with crypterHash: ${crypter.id.print()}
                            | with storedHash: ${crypterHash.print()}
                        """.trimMargin()
                    )
                } else {
                    val recalcTime = elem.getProperty<Long>("recalcTime")
                    val recalcTrigger = elem.getProperty<Long>("recalcTrigger")
                    val blockMemSize = elem.getProperty<Long>("blockMemSize")
                    val blockLength = elem.getProperty<Long>("blockLength")
                    DataResult.Success(
                        LedgerParams(
                            crypter, recalcTime,
                            recalcTrigger, blockMemSize,
                            blockLength
                        )
                    )
                }
            } catch (e: Exception) {
                DataResult.QueryFailure(
                    e.message ?: "", e
                )
            }
        }

    internal val idLoader: (Hash, OElement) -> DataResult<LedgerId> =
        { algo, elem ->
            try {
                val uuid = UUID.fromString(
                    elem.getProperty<String>("uuid")
                )
                val timestamp = Instant.ofEpochSecond(
                    elem.getProperty<Long>("seconds"),
                    elem.getProperty<Int>("nanos").toLong()
                )
                val id = elem.getProperty<String>("id")
                val hash = Hash(elem.getProperty<ByteArray>("hash"))
                val crypter = crypters[base64encode(algo.hash)]
                if (crypter != null) {
                    val params = paramLoader(
                        crypter,
                        hash,
                        elem.getProperty<OElement>("params")
                    )
                    params.intoData {
                        LedgerId(uuid, timestamp, id, this)
                    }
                } else {
                    DataResult.UnregisteredCrypter(
                        "Unregistered Crypter at load ledgerId: ${algo.print()}"
                    )
                }
            } catch (e: Exception) {
                DataResult.QueryFailure(
                    e.message ?: "", e
                )
            }
        }


    @Suppress("UNCHECKED_CAST")
    internal fun <T : LedgerContract> getFromDefault(
        id: String
    ): DefaultLoadable<T> =
        BlockChainLoaders.defaults[id] as DefaultLoadable<T>

    @Suppress("UNCHECKED_CAST")
    internal fun <T : ServiceHandle> getFromChains(
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