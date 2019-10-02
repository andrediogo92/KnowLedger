package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.knowledger.ledger.core.config.GlobalLedgerConfiguration.OTHER_BASE
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import java.math.BigDecimal

@Serializable
@SerialName("OtherData")
data class OtherData(
    val data: java.io.Serializable
) : LedgerData {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(serializer(), this)

    override val dataConstant: Long
        get() = OTHER_BASE


    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ONE

}
