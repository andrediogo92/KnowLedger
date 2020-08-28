@file:UseSerializers(HashSerializer::class, HashAlgorithmSerializer::class)

package org.knowledger.ledger.crypto.storage

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash
import org.knowledger.ledger.crypto.hash.Hashers
import org.knowledger.ledger.crypto.serial.HashAlgorithmSerializer

@Serializable
data class ImmutableMerkleTree(
    override val hashers: Hashers,
    override val collapsedTree: List<Hash>,
    override val levelIndex: List<Int>,
) : MerkleTree