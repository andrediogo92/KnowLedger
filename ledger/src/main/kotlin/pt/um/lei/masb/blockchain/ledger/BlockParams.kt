package pt.um.lei.masb.blockchain.ledger

import com.orientechnologies.orient.core.record.OElement
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.utils.Crypter
import pt.um.lei.masb.blockchain.utils.Hashable

data class BlockParams(
    val blockMemSize: Long = 2097152,
    val blockLength: Long = 512
) : Storable, Hashable, LedgerContract {
    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("BlockParams")
            .apply {
                setProperty("blockMemSize", blockMemSize)
                setProperty("blockLength", blockLength)
            }

    override fun digest(c: Crypter): Hash =
        c.applyHash(
            """
                $blockMemSize
                $blockLength
            """.trimIndent()
        )

}