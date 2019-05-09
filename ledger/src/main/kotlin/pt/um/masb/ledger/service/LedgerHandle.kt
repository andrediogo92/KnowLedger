package pt.um.masb.ledger.service

import com.orientechnologies.orient.core.id.ORID
import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.common.storage.results.QueryResult
import pt.um.masb.ledger.config.LedgerId
import pt.um.masb.ledger.config.LedgerParams
import pt.um.masb.ledger.results.intoLedger
import pt.um.masb.ledger.service.results.LedgerListResult
import pt.um.masb.ledger.service.results.LedgerResult
import pt.um.masb.ledger.storage.transactions.PersistenceWrapper


/**
 * Create a geographically unbounded ledger.
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
