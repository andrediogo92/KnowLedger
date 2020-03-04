package org.knowledger.ledger.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.PrimitiveDescriptor
import kotlinx.serialization.PrimitiveKind
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.data.DummyData

internal object DummyDataSerializer : KSerializer<DummyData> {
    override val descriptor: SerialDescriptor =
        PrimitiveDescriptor("DummyData", PrimitiveKind.BYTE)

    override fun deserialize(decoder: Decoder): DummyData =
        DummyData.also {
            assert(decoder.decodeByte() == 0xCC.toByte())
        }

    override fun serialize(encoder: Encoder, value: DummyData) =
        encoder.encodeByte(0xCC.toByte())
}