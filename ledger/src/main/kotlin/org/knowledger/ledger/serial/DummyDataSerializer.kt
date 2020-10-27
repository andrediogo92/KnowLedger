package org.knowledger.ledger.serial

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.knowledger.ledger.data.DummyData

internal object DummyDataSerializer : KSerializer<DummyData> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("DummyData", PrimitiveKind.BYTE)

    override fun deserialize(decoder: Decoder): DummyData =
        DummyData.also {
            assert(decoder.decodeByte() == 0xCC.toByte())
        }

    override fun serialize(encoder: Encoder, value: DummyData) =
        encoder.encodeByte(0xCC.toByte())
}