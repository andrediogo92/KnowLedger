package pt.um.lei.masb.blockchain.persistance

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.data.*

object LoaderManager {

    private val defaults: Loader =
        makeDefaults()

    private val loaders: Loaders =
        getStoredLoaders() ?: mutableMapOf()


    private fun makeDefaults(): Loader {
        return Loader(
            mutableMapOf(
                "Dummy" to ::loadDummy,
                "Humidity" to ::loadHumidity,
                "Luminosity" to ::loadLuminosity,
                "Noise" to ::loadNoise,
                "Other" to ::loadOther,
                "Temperature" to ::loadTemperature,
                "Block" to ::loadBlock,
                "BlockChain" to ::loadBlockChain,
                "BlockHeader" to ::loadBlockHeader,
                "Coinbase" to ::loadCoinbase,
                "SideChain" to ::loadSideChain,
                "Transaction" to ::loadTransaction,
                "TransactionOutput" to ::loadTransactionOutput
            )
        )
    }


    private fun getStoredLoaders(): Loaders? {
        val record =
            PersistenceWrapper.executeInSessionAndReturn {
                it.query("")//TODO Write query for loader
            }
        return if (record.hasNext()) {
            record.next().element.map {
                getFromRecord(it)
            }.orElse(null)
        } else {
            null
        }
    }


    private fun getFromRecord(it: OElement?): Loaders {
        //TODO get records from the element
        TODO()
    }


    @Suppress("UNCHECKED_CAST")
    internal fun <T> getFromDefault(id: String): Loadable<T> =
        defaults.loaders[id] as Loadable<T>
}