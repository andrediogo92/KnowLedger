package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.DEFAULT_CRYPTER
import pt.um.lei.masb.blockchain.utils.Hashable

data class LedgerParams(
    val crypter: Crypter = DEFAULT_CRYPTER,
    val recalcTime: Long = 1228800000,
    val recalcTrigger: Long = 2048,
    val blockMemSize: Long = 2097152,
    val blockLength: Long = 512
) : Storable, Hashable, LedgerContract {
    override fun store(session: NewInstanceSession): OElement =
        session
            .newInstance("LedgerParams")
            .apply {
                setProperty("crypter", crypter.id)
                setProperty("recalcTime", recalcTime)
                setProperty("recalcTrigger", recalcTrigger)
                setProperty("blockMemSize", blockMemSize)
                setProperty("blockLength", blockLength)
            }


    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
                ${crypter.id}
                $recalcTime
                $recalcTrigger
                $blockMemSize
                $blockLength
            """.trimIndent()
        )
}