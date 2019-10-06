package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.SerialName
import org.knowledger.ledger.serial.DummyDataSerializer
import java.math.BigDecimal

/**
 * Dummy value type used for the origin block.
 */
@SerialName("DummyData")
@PublishedApi
internal object DummyData : LedgerData {
    override fun clone(): DummyData = this

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(DummyDataSerializer, this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO
}