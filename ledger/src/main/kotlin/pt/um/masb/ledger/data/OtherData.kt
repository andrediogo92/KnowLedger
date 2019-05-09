package pt.um.masb.ledger.data

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import com.squareup.moshi.JsonClass
import pt.um.masb.common.Hash
import pt.um.masb.common.crypt.Crypter
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.DataDefaults
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.database.NewInstanceSession
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class OtherData(
    val data: java.io.Serializable
) : BlockChainData {
    override fun digest(c: Crypter): Hash {
        val bao = ByteArrayOutputStream(256)
        ObjectOutputStream(bao).use {
            it.writeObject(data)
        }
        return c.applyHash(
            bao.toByteArray()
        )
    }


    override val dataConstant: Int
        get() = DataDefaults.DEFAULT_UNKNOWN

    override fun store(
        session: NewInstanceSession
    ): OElement =
        session
            .newInstance("Other")
            .apply {
                val byteStream = ByteArrayOutputStream(
                    approximateSize.toInt()
                )
                ObjectOutputStream(
                    byteStream
                ).use {
                    it.writeObject(data)
                    val blob = ORecordBytes(
                        byteStream.toByteArray()
                    )
                    setProperty("data", blob)
                }
            }

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ONE

}
