package org.knowledger.testing.ledger

import kotlinx.serialization.Serializable
import org.knowledger.testing.core.random

@Serializable
data class SmallData(
    val index: Long = random.nextLong(),
    val nonce: Long = random.nextLong(),
) : Comparable<SmallData> {
    override fun compareTo(other: SmallData): Int =
        index.compareTo(other.index)
}