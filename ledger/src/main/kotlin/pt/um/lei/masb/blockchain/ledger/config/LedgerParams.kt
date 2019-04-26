package pt.um.lei.masb.blockchain.ledger.config

import com.orientechnologies.orient.core.record.OElement
import com.squareup.moshi.JsonClass
import pt.um.lei.masb.blockchain.ledger.Hash
import pt.um.lei.masb.blockchain.ledger.Hashable
import pt.um.lei.masb.blockchain.ledger.LedgerContract
import pt.um.lei.masb.blockchain.ledger.crypt.Crypter
import pt.um.lei.masb.blockchain.ledger.crypt.SHA256Encrypter
import pt.um.lei.masb.blockchain.persistance.Storable
import pt.um.lei.masb.blockchain.persistance.database.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.bytes
import pt.um.lei.masb.blockchain.utils.flattenBytes

@JsonClass(generateAdapter = true)
data class LedgerParams(
    val crypter: Crypter = SHA256Encrypter,
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