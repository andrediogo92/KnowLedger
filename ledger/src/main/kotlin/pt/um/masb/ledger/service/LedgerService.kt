package pt.um.masb.ledger.service

import mu.KLogging
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.ManagedDatabase
import pt.um.masb.common.database.StorageElement
import pt.um.masb.common.database.orient.OrientDatabase
import pt.um.masb.common.database.orient.OrientDatabaseInfo
import pt.um.masb.common.hash.AvailableHashAlgorithms
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import pt.um.masb.common.misc.stringToPrivateKey
import pt.um.masb.common.misc.stringToPublicKey
import pt.um.masb.common.storage.adapters.AbstractStorageAdapter
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.data.adapters.DummyDataStorageAdapter
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.service.adapters.LedgerHandleStorageAdapter
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper
import java.security.KeyPair

data class LedgerService(
    private val db: ManagedDatabase =
        OrientDatabase(OrientDatabaseInfo()),
    private val hasher: Hasher =
        AvailableHashAlgorithms.SHA256Hasher
) {
    private val session = db.newManagedSession()
    private val pw = PersistenceWrapper(session)


    fun getIdentById(id: String): Identity? {
        val ident: StorageElement? = pw.getIdent(id)
        return if (ident != null) {
            val keyPair = KeyPair(
                stringToPublicKey(ident.getStorageProperty("publicKey")),
                stringToPrivateKey(ident.getStorageProperty("privateKey"))
            )
            Identity(id, keyPair)
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
        crypterHash: Hash = AvailableHashAlgorithms.SHA256Hasher.id,
        hash: Hash
    ): LedgerResult<LedgerHandle> =
        pw.getBlockChain(crypterHash, hash)

    fun newLedgerHandle(
        id: String
    ): LedgerResult<LedgerHandle> {
        val ledgerHandle = LedgerHandle(pw, id)
        return pw.persistEntity(
            ledgerHandle, LedgerHandleStorageAdapter()
        ).intoLedger {
            ledgerHandle
        }
    }


    companion object : KLogging() {
        private val dataAdapters =
            mutableSetOf<AbstractStorageAdapter<out BlockChainData>>(
                DummyDataStorageAdapter()
            )


        fun getStorageAdapter(
            dataName: String
        ): AbstractStorageAdapter<out BlockChainData>? =
            dataAdapters.find {
                it.id == dataName
            }

        fun getStorageAdapter(
            clazz: Class<out BlockChainData>
        ): AbstractStorageAdapter<out BlockChainData>? =
            dataAdapters.find {
                it.clazz == clazz
            }

        fun addStorageAdapter(
            adapter: AbstractStorageAdapter<out BlockChainData>
        ): Boolean =
            dataAdapters.add(adapter)

    }
}