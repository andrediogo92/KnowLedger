package org.knowledger.ledger.data

import kotlinx.serialization.BinaryFormat
import org.knowledger.ledger.serial.DummyDataSerializer
import org.knowledger.ledger.storage.LedgerData
import org.knowledger.ledger.storage.SelfInterval
import java.math.BigDecimal

/**
 * Dummy value type used for the origin block.
 */
internal object DummyData : LedgerData {
    override fun clone(): DummyData = this

    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.dump(DummyDataSerializer, this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO
}