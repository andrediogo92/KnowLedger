@file:UseSerializers(HashSerializer::class)

package org.knowledger.ledger.chain.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import org.knowledger.ledger.core.serial.HashSerializer
import org.knowledger.ledger.crypto.Hash

@Serializable
data class TransactionHashes(val blockHash: Hash, val txHash: Hash, val txIndex: Int)
