package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.config.LedgerId
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.ledger.crypt.SHA256Encrypter
import pt.um.lei.masb.blockchain.persistance.database.ManagedDatabase
import pt.um.lei.masb.blockchain.persistance.database.ManagedDatabaseInfo
import pt.um.lei.masb.blockchain.persistance.database.PluggableDatabase
import pt.um.lei.masb.blockchain.persistance.loaders.Loadable
import pt.um.lei.masb.blockchain.persistance.loaders.Loaders
import pt.um.lei.masb.blockchain.persistance.loaders.PluggableLoaders
import pt.um.lei.masb.blockchain.persistance.transactions.PersistenceWrapper
import pt.um.lei.masb.blockchain.results.intoLedger
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.utils.base64encode
import pt.um.lei.masb.blockchain.utils.stringToPrivateKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
import java.security.KeyPair

class LedgerService(
    private val db: ManagedDatabase =
        PluggableDatabase(ManagedDatabaseInfo()),
    private val crypter: Crypter = SHA256Encrypter
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


    //Get a specific blockchain from DB
    fun getLedgerHandleById(
        id: LedgerId
    ): LedgerResult<LedgerHandle> =
        pw.getBlockChain(id)

    fun getLedgerHandleByHash(
        crypterHash: Hash = SHA256Encrypter.id,
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
            base64encode(SHA256Encrypter.id) to SHA256Encrypter
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