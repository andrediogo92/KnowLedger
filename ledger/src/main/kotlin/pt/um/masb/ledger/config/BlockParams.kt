package pt.um.masb.ledger.config

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.masb.common.Hash
import pt.um.masb.common.Hashable
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.LedgerContract

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