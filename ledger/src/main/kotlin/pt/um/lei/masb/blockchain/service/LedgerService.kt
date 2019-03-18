package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.data.Loadable
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.LedgerId
import pt.um.lei.masb.blockchain.persistance.ManagedDatabase
import pt.um.lei.masb.blockchain.persistance.ManagedDatabaseInfo
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.PluggableDatabase
import pt.um.lei.masb.blockchain.persistance.loaders.Loaders
import pt.um.lei.masb.blockchain.persistance.loaders.PluggableLoaders
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.base64encode
import pt.um.lei.masb.blockchain.utils.intoLedger
import pt.um.lei.masb.blockchain.utils.stringToPrivateKey
import pt.um.lei.masb.blockchain.utils.stringToPublicKey
import java.security.KeyPair

class LedgerService(
    private val db: ManagedDatabase =
        PluggableDatabase(ManagedDatabaseInfo()),
    private val crypter: Crypter = DEFAULT_CRYPTER
) {
    private val session = db.newManagedSession()
    private val pw = PersistenceWrapper(session)


    fun getIdentById(id: String): Ident? =
        let {
            val ident: OElement? = pw.getIdent(id)
            if (ident != null) {
                Ident(
                    id,
                    KeyPair(
                        stringToPublicKey(ident.getProperty("publicKey")),
                        stringToPrivateKey(ident.getProperty("privateKey"))
                    )
                )
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
        crypterHash: Hash = DEFAULT_CRYPTER.id,
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
        val crypters = mutableMapOf(
            base64encode(DEFAULT_CRYPTER.id) to DEFAULT_CRYPTER
        )

        private val dataLoaders: MutableMap<Hash, Loaders> =
            mutableMapOf()


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
}