package org.knowledger.ledger.storage.mutations

import org.knowledger.ledger.storage.TransactionOutput
import org.knowledger.ledger.storage.witness.Witness


internal fun Witness.calculateSize(hashSize: Int): Int =
    transactionOutputs.calculateTransactionOutputsSize(hashSize) +
    hashSize * 3 + publicKey.bytes.size + Int.SIZE_BYTES

internal fun calculateTXOSize(hashSize: Int): Int = Int.SIZE_BYTES * 2 + hashSize * 4

internal fun Iterable<Witness>.calculateWitnessesSize(hashSize: Int): Int =
    sumBy { it.calculateSize(hashSize) }

internal fun Iterable<TransactionOutput>.calculateTransactionOutputsSize(hashSize: Int): Int =
    sumBy { calculateTXOSize(hashSize) }
