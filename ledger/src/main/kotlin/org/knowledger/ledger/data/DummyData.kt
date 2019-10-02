package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.serial.DummyDataSerializer
import java.math.BigDecimal

/**
 * Dummy value type used for the origin block.
 */
@SerialName("DummyData")
object DummyData : LedgerData {
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(DummyDataSerializer, this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO
}