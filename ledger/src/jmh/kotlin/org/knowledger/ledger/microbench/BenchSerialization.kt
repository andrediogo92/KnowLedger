package org.knowledger.ledger.microbench

import kotlinx.serialization.UnstableDefault
import org.knowledger.ledger.results.unwrap
import org.knowledger.ledger.serial.LedgerSerializer
import org.knowledger.ledger.serial.ledgerBinarySerializer
import org.knowledger.ledger.service.Identity
import org.knowledger.ledger.storage.Block
import org.knowledger.ledger.storage.Transaction
import org.knowledger.ledger.test.generateBlock
import org.knowledger.ledger.test.generateXTransactionsArray
import org.knowledger.testing.ledger.testEncoder
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import java.util.concurrent.TimeUnit

@UnstableDefault
@Warmup(
    iterations = 10, time = 500,
    timeUnit = TimeUnit.MILLISECONDS, batchSize = 2
)
@Measurement(
    iterations = 75, time = 500,
    timeUnit = TimeUnit.MILLISECONDS, batchSize = 5
)
@BenchmarkMode(Mode.All)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
open class BenchSerialization {
    @Benchmark
    fun serializationByPrettySerializer(
        state: SerializationState, blackhole: Blackhole
    ) {
        blackhole.consume(
            state.binarySerializer.encodeBlock(state.block)
        )
    }

    @Benchmark
    fun serializationByByteSerializer(
        state: SerializationState, blackhole: Blackhole
    ) {
        blackhole.consume(
            state.binarySerializer.encodeCompactBlock(state.block)
        )
    }
}


@State(Scope.Thread)
open class SerializationState(
    val id: Array<Identity> = arrayOf(
        Identity("boy"), Identity("wonder")
    ),
    val ts: Array<Transaction> =
        generateXTransactionsArray(id, 100),
    val block: Block =
        generateBlock(id, ts),
    val binarySerializer: LedgerSerializer.Binary =
        ledgerBinarySerializer { encoder = testEncoder }.unwrap()
)