@file:UseSerializers(HashSerializer::class)

package org.knowledger.testing.ledger

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.encoding.base64.base64Encoded
import org.knowledger.ledger.core.data.LedgerData
import org.knowledger.ledger.core.data.SelfInterval
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.testing.core.random
import java.math.BigDecimal

@Serializable
data class RandomData(
    val randomLongs: List<Long>,
    val randomStrings: List<String>,
    val randomHashes: List<Hash>,
    val index: Long = random.nextLong(),
) : LedgerData, Comparable<RandomData> {
    constructor(stringFactor: Int, size: Int, index: Long = random.nextLong()) : this(
        random.randomLongs().take(size).toList(),
        random.randomByteArrays(stringFactor).map(ByteArray::base64Encoded).take(size).toList(),
        random.random256Hashes().take(size).toList(), index
    )

    override fun clone(): LedgerData = copy()

    override fun calculateDiff(previous: SelfInterval): BigDecimal = BigDecimal.ONE

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: BinaryFormat): ByteArray =
        encoder.encodeToByteArray(serializer(), this)

    override fun compareTo(other: RandomData): Int =
        index.compareTo(other.index)
}