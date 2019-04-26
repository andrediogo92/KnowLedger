package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.config.LedgerId
import pt.um.lei.masb.blockchain.ledger.config.LedgerParams
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.results.QueryResult
import pt.um.lei.masb.blockchain.persistance.transactions.PersistenceWrapper
import pt.um.lei.masb.blockchain.results.intoLedger
import pt.um.lei.masb.blockchain.service.results.LedgerListResult
import pt.um.lei.masb.blockchain.service.results.LedgerResult


/**
 * Create a geographically unbounded blockchain.
 */
class LedgerHandle internal constructor(
    private val pw: PersistenceWrapper,
    val ledgerId: LedgerId
) : Storable, ServiceHandle {
    //TODO: efficiently retrieve chains registered for this ledger.
    val knownChainTypes: QueryResult<List<String>>
        get() = pw.getKnownChainHandleTypes(
            ledgerId.hashId
        )

    internal val knownChainIDs: QueryResult<List<ORID>>
        get() = pw.getKnownChainHandleIDs(
            ledgerId.hashId
        )

    val knownChains: LedgerListResult<ChainHandle>
        get() = pw.getKnownChainHandles(
            ledgerId.params.crypter.id,
            ledgerId.hashId
        )

    init {
        pw.registerDefaultClusters(
            ledgerId.hashId
        )
    }


    internal constructor(
        pw: PersistenceWrapper,
        id: String
    ) : this(
        pw,
        LedgerId(id)
    )

    internal constructor(
        pw: PersistenceWrapper,
        id: String,
        params: LedgerParams
    ) : this(
        pw,
        LedgerId(id, params = params)
    )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        ledgerId.store(session)


    fun <T : BlockChainData> getChainHandleOf(
        clazz: Class<T>
    ): LedgerResult<ChainHandle> =
        pw.getChainHandle(
            ledgerId.params.crypter.id,
            clazz,
            ledgerId.hashId
        )
 
    fun <T : BlockChainData> registerNewChainHandleOf(
        clazz: Class<T>
    ): LedgerResult<ChainHandle> =
        ChainHandle(
            pw, ledgerId.params,
            clazz.name, ledgerId.hashId
        ).let {
            pw.tryAddChainHandle(it).intoLedger {
                it
            }
        }

    companion object : KLogging()
}
