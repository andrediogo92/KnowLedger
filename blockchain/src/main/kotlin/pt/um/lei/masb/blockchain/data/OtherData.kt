package pt.um.lei.masb.blockchain.data

import com.orientechnologies.orient.core.record.OElement
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.record.impl.ORecordBytes
import java.io.ByteArrayOutputStream
import java.io.ObjectOutputStream
import java.math.BigDecimal

@kotlinx.serialization.Serializable
data class OtherData(
    val data: java.io.Serializable
) : BlockChainData {


    override val dataConstant: Int
        get() = DATA_DEFAULTS.DEFAULT_UNKNOWN

    override fun store(): OElement =
        ODocument("Other").let { doc ->
            val byteStream =
                ByteArrayOutputStream(approximateSize.toInt())
            try {
                val oos =
                    ObjectOutputStream(byteStream)
                oos.writeObject(data)
                val blob = ORecordBytes(byteStream.toByteArray())
                doc.setProperty("data", blob)
            } catch (ex: Exception) {
                logger.error(ex) {
                    "Serialization error for $data"
                }
                doc.setProperty("data", ByteArray(1))
            }
            doc
        }

    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        BigDecimal.ONE

    override fun toString(): String =
        "OtherData(data=$data)"
}
