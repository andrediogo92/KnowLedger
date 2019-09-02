package org.knowledger.ledger.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.StringDescriptor
import kotlinx.serialization.withName
import org.knowledger.ledger.data.DummyData

@Serializer(forClass = DummyData::class)
object DummyDataSerializer : KSerializer<DummyData> {
    override val descriptor: SerialDescriptor
        get() = StringDescriptor.withName("DummyData")

    override fun deserialize(decoder: Decoder): DummyData {
        assert(decoder.decodeByte() == 0xCC.toByte())
        return DummyData
    }

    override fun serialize(encoder: Encoder, obj: DummyData) =
        encoder.encodeByte(0xCC.toByte())
}