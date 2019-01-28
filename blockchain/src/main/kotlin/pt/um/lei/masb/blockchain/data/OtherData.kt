package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import pt.um.lei.masb.blockchain.Hash
import pt.um.lei.masb.blockchain.persistance.NewInstanceSession
import pt.um.lei.masb.blockchain.utils.Crypter
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal

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
            .let { doc ->
                val byteStream =
                    ByteArrayOutputStream(
                        approximateSize.toInt()
                    )
                ObjectOutputStream(
                    byteStream
                ).use {
                    it.writeObject(data)
                    val blob = ORecordBytes(
                        byteStream.toByteArray()
                    )
                    doc.setProperty(
                        "data",
                        blob
                    )
                }
                doc
            }

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ONE

    override fun toString(): String =
        "OtherData(data = $data)"
}
