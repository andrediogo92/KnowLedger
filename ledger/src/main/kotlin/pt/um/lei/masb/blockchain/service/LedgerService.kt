package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.LedgerId
import pt.um.lei.masb.blockchain.persistance.ManagedDatabase
import pt.um.lei.masb.blockchain.persistance.ManagedDatabaseInfo
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.PluggableDatabase
import pt.um.lei.masb.blockchain.service.results.LedgerResult
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
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
    ): LedgerResult =
        pw.getBlockChain(id)

    fun getLedgerHandleByHash(
        hash: Hash
    ): LedgerResult =
        pw.getBlockChain(hash)

}