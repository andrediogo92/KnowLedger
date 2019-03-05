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
import java.time.Instant
import java.util.*


/**
 * Create a geographically unbounded blockchain.
 */
class LedgerHandle internal constructor(
    private val pw: PersistenceWrapper,
    val blockChainId: LedgerId
) : Storable, ServiceHandle {

    init {
        pw.registerDefaultClusters(
            blockChainId.hash
        )
    }


    internal constructor(
        pw: PersistenceWrapper,
        id: String
    ) : this(
        pw,
        LedgerId(
            UUID.randomUUID(),
            Instant.now(),
            id,
            LedgerParams()
        )
    )

    internal constructor(
        pw: PersistenceWrapper,
        id: String,
        params: LedgerParams
    ) : this(
        pw,
        LedgerId(
            UUID.randomUUID(),
            Instant.now(),
            id,
            params
        )
    )


    override fun store(
        session: NewInstanceSession
    ): OElement =
        blockChainId.store(session)


    fun <T : BlockChainData> getChainHandleOf(
        clazz: Class<T>
    ): LedgerResult<ChainHandle> =
        pw.getChainHandle()

    fun <T : BlockChainData> registerNewChainHandleOf(
        clazz: Class<T>
    ): LedgerResult<ChainHandle> =
        pw.tryAddChainHandle()

    companion object : KLogging()
}
