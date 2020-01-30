package org.knowledger.ledger.microbench

import kotlinx.serialization.UnstableDefault
import org.knowledger.ledger.serial.BlockSerializer
import org.knowledger.ledger.serial.internal.BlockByteSerializer
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
            testEncoder.load(
                BlockSerializer,
                testEncoder.dump(BlockSerializer, state.block)
            )
        )
    }

    @Benchmark
    fun serializationByByteSerializer(
        state: SerializationState, blackhole: Blackhole
    ) {
        blackhole.consume(
            testEncoder.load(
                BlockByteSerializer,
                testEncoder.dump(BlockByteSerializer, state.block)
            )
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
        generateBlock(id, ts)
)