@file:UseSerializers(HashSerializer::class)

package org.knowledger.testing.ledger

import kotlinx.serialization.BinaryFormat
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
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
    val index: Long = random.randomLong()
) : LedgerData, Comparable<RandomData> {
    constructor(
        stringFactor: Int, size: Int,
        index: Long = random.randomLong()
    ) : this(
        random.randomLongs().take(size).toList(),
        random.randomStrings(stringFactor).take(size).toList(),
        random.random256Hashes().take(size).toList(), index
    )

    override fun clone(): LedgerData =
        copy()

    override fun calculateDiff(previous: SelfInterval): BigDecimal =
        BigDecimal.ONE

    override fun serialize(encoder: BinaryFormat): ByteArray =
        ByteArray(0)

    override fun compareTo(other: RandomData): Int =
        index.compareTo(other.index)
}