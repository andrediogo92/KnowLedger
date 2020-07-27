package org.knowledger.ledger.microbench

import org.knowledger.collections.MutableSortedList
import org.knowledger.ledger.results.unwrapFailure
import org.knowledger.ledger.storage.MutableTransaction
import org.knowledger.ledger.storage.block.ImmutableBlock
import org.knowledger.ledger.storage.immutableCopy
import org.knowledger.ledger.storage.serial.LedgerSerializer
import org.knowledger.ledger.storage.serial.ledgerBinarySerializer
import org.knowledger.testing.storage.defaultCbor
import org.knowledger.testing.storage.generateBlockWithChain
import org.knowledger.testing.storage.generateXTransactions
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class SerializationState : BaseState() {
    private val ts: MutableSortedList<MutableTransaction> =
        generateXTransactions(id, 100)
    val block: ImmutableBlock = generateBlockWithChain(ts, chainId).immutableCopy()
    val binarySerializer: LedgerSerializer.Binary =
        ledgerBinarySerializer { encoder = defaultCbor }.unwrapFailure()
}
