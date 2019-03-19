package pt.um.lei.masb.blockchain.service

import com.orientechnologies.orient.core.record.OElement
import mu.KLogging
import pt.um.lei.masb.blockchain.data.BlockChainData
import pt.um.lei.masb.blockchain.ledger.LedgerId
import pt.um.lei.masb.blockchain.ledger.LedgerParams
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.PersistenceWrapper
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.service.results.LedgerResult


/**
 * Create a geographically unbounded blockchain.
 */
class LedgerHandle internal constructor(
    private val pw: PersistenceWrapper,
    val ledgerId: LedgerId
) : Storable, ServiceHandle {
    //TODO: efficiently retrieve chains registered for this ledger.
    val knownChainTypes: List<Class<BlockChainData>> = emptyList()

    init {
        pw.registerDefaultClusters(
            ledgerId.hash
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
        pw.getChainHandle(clazz, ledgerId)
 
    fun <T : BlockChainData> registerNewChainHandleOf(
        clazz: Class<T>
    ): LedgerResult<ChainHandle> =
        pw.tryAddChainHandle(clazz, ledgerId)

    companion object : KLogging()
}
