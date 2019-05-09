package pt.um.masb.ledger.service

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.masb.common.Hash
import pt.um.masb.common.crypt.AvailableCrypters
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.ManagedDatabaseInfo
import pt.um.masb.common.database.PluggableDatabase
import pt.um.masb.common.misc.base64encode
import pt.um.masb.common.misc.stringToPrivateKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.storage.loaders.Loadable
import pt.um.masb.ledger.storage.loaders.Loaders
import pt.um.masb.ledger.storage.loaders.PluggableLoaders
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.security.KeyPair

class LedgerService(
    private val db: ManagedDatabase =
        PluggableDatabase(ManagedDatabaseInfo()),
    private val crypter: Crypter = AvailableCrypters.SHA256Encrypter
) {
    private val session = db.newManagedSession()
    private val pw = PersistenceWrapper(session)

    init {
        mutCrypters[base64encode(crypter.id)] = crypter
    }

    fun getIdentById(id: String): Ident? {
        val ident: OElement? = pw.getIdent(id)
        return if (ident != null) {
            val keyPair = KeyPair(
                stringToPublicKey(ident.getProperty("publicKey")),
                stringToPrivateKey(ident.getProperty("privateKey"))
            )
            Ident(id, keyPair)
        } else {
            null
        }
    }


    //Get a specific ledger from DB
    fun getLedgerHandleById(
        id: LedgerId
    ): LedgerResult<LedgerHandle> =
        pw.getBlockChain(id)

    fun getLedgerHandleByHash(
        crypterHash: Hash = AvailableCrypters.SHA256Encrypter.id,
        hash: Hash
    ): LedgerResult<LedgerHandle> =
        pw.getBlockChain(crypterHash, hash)

    fun newLedgerHandle(
        id: String
    ): LedgerResult<LedgerHandle> {
        val ledgerHandle = LedgerHandle(pw, id)
        return pw.persistEntity(ledgerHandle).intoLedger {
            ledgerHandle
        }
    }


    companion object : KLogging() {
        private val mutCrypters: MutableMap<String, Crypter> = mutableMapOf(
            base64encode(AvailableCrypters.SHA256Encrypter.id) to AvailableCrypters.SHA256Encrypter
        )

        val crypters: Map<String, Crypter>
            get() = mutCrypters

        private val dataLoaders: MutableMap<Hash, Loaders> =
            mutableMapOf()


        @Suppress("UNCHECKED_CAST")
        internal fun getFromLoaders(
            blockChainId: Hash,
            id: String
        ): Loadable<out BlockChainData>? =
            dataLoaders[blockChainId]
                ?.loaders
                ?.get(id)

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
            vararg entries: Pair<String, Loadable<out BlockChainData>>
        ) {
            if (dataLoaders.containsKey(blockChainId)) {
                entries.forEach { pair ->
                    dataLoaders[blockChainId]!!.loaders[pair.first] = pair.second
                }
            } else {
                val m: MutableMap<String, Loadable<out BlockChainData>> = mutableMapOf()
                entries.forEach { pair ->
                    m[pair.first] = pair.second
                }
                dataLoaders[blockChainId] =
                    PluggableLoaders(m)
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
}