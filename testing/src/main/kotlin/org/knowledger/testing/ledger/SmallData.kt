package org.knowledger.testing.ledger

import kotlinx.serialization.Serializable
import org.knowledger.testing.core.random

@Serializable
data class SmallData(
    val index: Long = random.randomLong(), val nonce: Long = random.randomLong(),
) : Comparable<SmallData> {
    override fun compareTo(other: SmallData): Int =
        index.compareTo(other.index)
}