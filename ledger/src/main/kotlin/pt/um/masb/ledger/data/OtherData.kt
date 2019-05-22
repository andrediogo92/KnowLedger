package pt.um.masb.ledger.data

import com.squareup.moshi.JsonClass
import pt.um.masb.common.data.BlockChainData
import pt.um.masb.common.data.DataDefaults
import pt.um.masb.common.data.SelfInterval
import pt.um.masb.common.hash.Hash
import pt.um.masb.common.hash.Hasher
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal

@JsonClass(generateAdapter = true)
data class OtherData(
    val data: java.io.Serializable
) : BlockChainData {
    override fun digest(c: Hasher): Hash {
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


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ONE

}