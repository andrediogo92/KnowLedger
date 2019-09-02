package org.knowledger.ledger.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.cbor.Cbor
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.serial.DummyDataSerializer
import java.math.BigDecimal

/**
 * Dummy value type used for the origin block.
 */
@SerialName("DummyData")
object DummyData : LedgerData {
    override fun serialize(cbor: Cbor): ByteArray =
        cbor.dump(DummyDataSerializer, this)

    override fun calculateDiff(
        previous: SelfInterval
    ): BigDecimal =
        BigDecimal.ZERO
}