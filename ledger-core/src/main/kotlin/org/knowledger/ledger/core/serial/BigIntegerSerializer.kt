package org.knowledger.ledger.core.serial

import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import org.knowledger.ledger.core.misc.bytesFromHexString
import org.knowledger.ledger.core.misc.toHexString
import java.math.BigInteger

@Serializer(forClass = BigInteger::class)
object BigIntegerSerializer : KSerializer<BigInteger> {
    override fun deserialize(
        decoder: Decoder
    ): BigInteger =
        BigInteger(decoder.decodeString().bytesFromHexString())

    override fun serialize(
        encoder: Encoder, obj: BigInteger
    ) =
        encoder.encodeString(obj.toByteArray().toHexString())
}