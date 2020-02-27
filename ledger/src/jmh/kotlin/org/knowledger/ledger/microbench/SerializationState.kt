package org.knowledger.ledger.microbench

import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.serial.LedgerSerializer
import org.knowledger.ledger.serial.ledgerBinarySerializer
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.test.generateBlock
import org.knowledger.ledger.test.generateXTransactionsArray
import org.knowledger.testing.ledger.testEncoder
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State

@State(Scope.Benchmark)
open class SerializationState : BaseState() {
    val ts: Array<Transaction> =
        generateXTransactionsArray(id, 100)
    val block: Block =
        generateBlock(id, ts)
    val binarySerializer: LedgerSerializer.Binary =
        ledgerBinarySerializer { encoder = testEncoder }.unwrap()
}
