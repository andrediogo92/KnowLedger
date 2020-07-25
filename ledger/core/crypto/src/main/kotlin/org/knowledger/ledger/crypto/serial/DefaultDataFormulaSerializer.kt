package org.knowledger.ledger.crypto.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import org.knowledger.ledger.core.data.DefaultDiff
import org.knowledger.ledger.core.data.hash.classDigest
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.hash.Hashers

object DefaultDataFormulaSerializer : KSerializer<DefaultDiff> {
    override val descriptor: SerialDescriptor =
        HashSerializer.descriptor

    override fun deserialize(decoder: Decoder): DefaultDiff =
        decoder.decodeSerializableValue(HashSerializer).let {
            assert(it == classDigest<DefaultDiff>(Hashers.SHA3512Hasher))
            DefaultDiff
        }

    override fun serialize(encoder: Encoder, value: DefaultDiff) {
        encoder.encodeSerializableValue(
            HashSerializer, classDigest<DefaultDiff>(Hashers.SHA3512Hasher)
        )
    }
}