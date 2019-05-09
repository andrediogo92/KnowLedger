package pt.um.masb.ledger.config

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.masb.common.Hash
import pt.um.masb.common.Hashable
import pt.um.masb.common.crypt.AvailableCrypters
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.database.NewInstanceSession
import pt.um.masb.common.misc.bytes
import pt.um.masb.common.misc.flattenBytes
import pt.um.masb.common.storage.adapters.Storable
import pt.um.masb.ledger.LedgerContract

@JsonClass(generateAdapter = true)
data class LedgerParams(
    val crypter: Crypter = AvailableCrypters.SHA256Encrypter,
    val recalcTime: Long = 1228800000,
    val recalcTrigger: Long = 2048,
    val blockParams: BlockParams = BlockParams()
) : Storable, Hashable, LedgerContract {
    override fun store(session: NewInstanceSession): OElement =
        session
            .newInstance("LedgerParams")
            .apply {
                setProperty("crypter", crypter.id)
                setProperty("recalcTime", recalcTime)
                setProperty("recalcTrigger", recalcTrigger)
                setProperty("blockParams", blockParams.store(session))
            }


    override fun digest(c: Crypter): Hash =
        c.applyHash(
            flattenBytes(
                crypter.id,
                recalcTime.bytes(),
                recalcTrigger.bytes(),
                blockParams.blockMemSize.bytes(),
                blockParams.blockLength.bytes()
            )
        )
}