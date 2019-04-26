package pt.um.lei.masb.blockchain.ledger.config

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.Hashable
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.bytes

@JsonClass(generateAdapter = true)
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
            blockMemSize.bytes() +
                    blockLength.bytes()
        )

}